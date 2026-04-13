package com.voltmonitor.repository;

import com.voltmonitor.model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:voltmonitor.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() throws SQLException {
        connection = DriverManager.getConnection(DB_URL);
        connection.setAutoCommit(true);
        inicializarBancoDeDados();
    }

    public static synchronized DatabaseManager getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void inicializarBancoDeDados() throws SQLException {
        String sqlUsuarios = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                sobrenome TEXT NOT NULL,
                cpf TEXT UNIQUE NOT NULL,
                matricula TEXT UNIQUE NOT NULL,
                funcao TEXT NOT NULL,
                login TEXT UNIQUE NOT NULL,
                senha_hash TEXT NOT NULL,
                ativo INTEGER DEFAULT 1
            )""";

        String sqlDepartamentos = """
            CREATE TABLE IF NOT EXISTS departamentos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT UNIQUE NOT NULL,
                descricao TEXT,
                ativo INTEGER DEFAULT 1
            )""";

        String sqlEquipamentos = """
            CREATE TABLE IF NOT EXISTS equipamentos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                marca TEXT NOT NULL,
                modelo TEXT NOT NULL,
                tensao_nominal REAL NOT NULL,
                ativo INTEGER DEFAULT 1
            )""";

        String sqlClientes = """
            CREATE TABLE IF NOT EXISTS clientes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo_pessoa TEXT NOT NULL,
                nome TEXT NOT NULL,
                sobrenome TEXT,
                documento TEXT UNIQUE NOT NULL,
                logradouro TEXT,
                numero TEXT,
                bairro TEXT,
                cidade TEXT,
                estado TEXT,
                cep TEXT,
                ddd TEXT,
                telefone TEXT,
                ip_local TEXT,
                monitorado INTEGER DEFAULT 1,
                equipamento_id INTEGER,
                ativo INTEGER DEFAULT 1,
                FOREIGN KEY (equipamento_id) REFERENCES equipamentos(id)
            )""";

        String sqlOrdensServico = """
            CREATE TABLE IF NOT EXISTS ordens_servico (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente_id INTEGER NOT NULL,
                nome_cliente TEXT NOT NULL,
                equipamento_id INTEGER,
                modelo_equipamento TEXT,
                motivo TEXT NOT NULL,
                status TEXT DEFAULT 'ABERTA',
                descricao TEXT,
                data_abertura TEXT NOT NULL,
                data_fechamento TEXT,
                departamento_destino TEXT DEFAULT 'Suporte Técnico',
                ultima_tensao REAL,
                situacao_rede TEXT,
                FOREIGN KEY (cliente_id) REFERENCES clientes(id)
            )""";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlDepartamentos);
            stmt.execute(sqlEquipamentos);
            stmt.execute(sqlClientes);
            stmt.execute(sqlOrdensServico);
        }

        // Criar admin padrão se não existir
        if (!existeAdminPadrao()) {
            criarAdminPadrao();
        }
        // Criar departamento Suporte Técnico padrão
        criarDepartamentoPadrao();
    }

    private boolean existeAdminPadrao() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE login = 'admin'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getInt(1) > 0;
        }
    }

    private void criarAdminPadrao() throws SQLException {
        String senhaHash = BCrypt.hashpw("admin123", BCrypt.gensalt());
        String sql = "INSERT INTO usuarios (nome, sobrenome, cpf, matricula, funcao, login, senha_hash) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "Administrador");
            ps.setString(2, "Sistema");
            ps.setString(3, "000.000.000-00");
            ps.setString(4, "ADM001");
            ps.setString(5, "Administrador");
            ps.setString(6, "admin");
            ps.setString(7, senhaHash);
            ps.executeUpdate();
        }
    }

    private void criarDepartamentoPadrao() throws SQLException {
        String sql = "INSERT OR IGNORE INTO departamentos (nome, descricao) VALUES ('Suporte Técnico', 'Departamento de suporte técnico')";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    // ===== USUARIOS =====
    public boolean autenticarUsuario(String login, String senha, Usuario[] result) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE login = ? AND ativo = 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hash = rs.getString("senha_hash");
                if (BCrypt.checkpw(senha, hash)) {
                    result[0] = mapUsuario(rs);
                    return true;
                }
            }
        }
        return false;
    }

    public List<Usuario> listarUsuarios() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE ativo = 1 ORDER BY nome";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapUsuario(rs));
        }
        return lista;
    }

    public void inserirUsuario(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, sobrenome, cpf, matricula, funcao, login, senha_hash) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getSobrenome());
            ps.setString(3, u.getCpf());
            ps.setString(4, u.getMatricula());
            ps.setString(5, u.getFuncao());
            ps.setString(6, u.getLogin());
            ps.setString(7, BCrypt.hashpw(u.getSenhaHash(), BCrypt.gensalt()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) u.setId(rs.getInt(1));
        }
    }

    public void excluirUsuario(int id) throws SQLException {
        String sql = "UPDATE usuarios SET ativo = 0 WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNome(rs.getString("nome"));
        u.setSobrenome(rs.getString("sobrenome"));
        u.setCpf(rs.getString("cpf"));
        u.setMatricula(rs.getString("matricula"));
        u.setFuncao(rs.getString("funcao"));
        u.setLogin(rs.getString("login"));
        u.setSenhaHash(rs.getString("senha_hash"));
        u.setAtivo(rs.getInt("ativo") == 1);
        return u;
    }

    // ===== CLIENTES =====
    public List<Cliente> listarClientes() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes WHERE ativo = 1 ORDER BY nome";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapCliente(rs));
        }
        return lista;
    }

    public List<Cliente> listarClientesMonitorados() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes WHERE ativo = 1 AND monitorado = 1 ORDER BY nome";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapCliente(rs));
        }
        return lista;
    }

    public void inserirCliente(Cliente c) throws SQLException {
        String sql = """
            INSERT INTO clientes (tipo_pessoa, nome, sobrenome, documento, logradouro, numero, bairro,
            cidade, estado, cep, ddd, telefone, ip_local, monitorado, equipamento_id)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getTipoPessoa());
            ps.setString(2, c.getNome());
            ps.setString(3, c.getSobrenome());
            ps.setString(4, c.getDocumento());
            ps.setString(5, c.getLogradouro());
            ps.setString(6, c.getNumero());
            ps.setString(7, c.getBairro());
            ps.setString(8, c.getCidade());
            ps.setString(9, c.getEstado());
            ps.setString(10, c.getCep());
            ps.setString(11, c.getDdd());
            ps.setString(12, c.getTelefone());
            ps.setString(13, c.getIpLocal());
            ps.setInt(14, c.isMonitorado() ? 1 : 0);
            ps.setInt(15, c.getEquipamentoId());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) c.setId(rs.getInt(1));
        }
    }

    public void excluirCliente(int id) throws SQLException {
        String sql = "UPDATE clientes SET ativo = 0 WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void atualizarMonitoramentoCliente(int id, boolean monitorado) throws SQLException {
        String sql = "UPDATE clientes SET monitorado = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, monitorado ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Cliente mapCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setTipoPessoa(rs.getString("tipo_pessoa"));
        c.setNome(rs.getString("nome"));
        c.setSobrenome(rs.getString("sobrenome"));
        c.setDocumento(rs.getString("documento"));
        c.setLogradouro(rs.getString("logradouro"));
        c.setNumero(rs.getString("numero"));
        c.setBairro(rs.getString("bairro"));
        c.setCidade(rs.getString("cidade"));
        c.setEstado(rs.getString("estado"));
        c.setCep(rs.getString("cep"));
        c.setDdd(rs.getString("ddd"));
        c.setTelefone(rs.getString("telefone"));
        c.setIpLocal(rs.getString("ip_local"));
        c.setMonitorado(rs.getInt("monitorado") == 1);
        c.setEquipamentoId(rs.getInt("equipamento_id"));
        c.setAtivo(rs.getInt("ativo") == 1);
        return c;
    }

    // ===== EQUIPAMENTOS =====
    public List<Equipamento> listarEquipamentos() throws SQLException {
        List<Equipamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM equipamentos WHERE ativo = 1 ORDER BY marca, modelo";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapEquipamento(rs));
        }
        return lista;
    }

    public Equipamento buscarEquipamentoPorId(int id) throws SQLException {
        String sql = "SELECT * FROM equipamentos WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapEquipamento(rs);
        }
        return null;
    }

    public void inserirEquipamento(Equipamento e) throws SQLException {
        String sql = "INSERT INTO equipamentos (marca, modelo, tensao_nominal) VALUES (?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getMarca());
            ps.setString(2, e.getModelo());
            ps.setDouble(3, e.getTensaoNominal());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) e.setId(rs.getInt(1));
        }
    }

    public void excluirEquipamento(int id) throws SQLException {
        String sql = "UPDATE equipamentos SET ativo = 0 WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Equipamento mapEquipamento(ResultSet rs) throws SQLException {
        Equipamento e = new Equipamento();
        e.setId(rs.getInt("id"));
        e.setMarca(rs.getString("marca"));
        e.setModelo(rs.getString("modelo"));
        e.setTensaoNominal(rs.getDouble("tensao_nominal"));
        e.setAtivo(rs.getInt("ativo") == 1);
        return e;
    }

    // ===== DEPARTAMENTOS =====
    public List<Departamento> listarDepartamentos() throws SQLException {
        List<Departamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM departamentos WHERE ativo = 1 ORDER BY nome";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapDepartamento(rs));
        }
        return lista;
    }

    public void inserirDepartamento(Departamento d) throws SQLException {
        String sql = "INSERT INTO departamentos (nome, descricao) VALUES (?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getNome());
            ps.setString(2, d.getDescricao());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) d.setId(rs.getInt(1));
        }
    }

    public void excluirDepartamento(int id) throws SQLException {
        String sql = "UPDATE departamentos SET ativo = 0 WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Departamento mapDepartamento(ResultSet rs) throws SQLException {
        Departamento d = new Departamento();
        d.setId(rs.getInt("id"));
        d.setNome(rs.getString("nome"));
        d.setDescricao(rs.getString("descricao"));
        d.setAtivo(rs.getInt("ativo") == 1);
        return d;
    }

    // ===== ORDENS DE SERVICO =====
    public List<OrdemServico> listarOrdensServico() throws SQLException {
        List<OrdemServico> lista = new ArrayList<>();
        String sql = "SELECT * FROM ordens_servico ORDER BY data_abertura DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapOrdemServico(rs));
        }
        return lista;
    }

    public int inserirOrdemServico(OrdemServico os) throws SQLException {
        String sql = """
            INSERT INTO ordens_servico (cliente_id, nome_cliente, equipamento_id, modelo_equipamento,
            motivo, status, descricao, data_abertura, departamento_destino, ultima_tensao, situacao_rede)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)""";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, os.getClienteId());
            ps.setString(2, os.getNomeCliente());
            ps.setInt(3, os.getEquipamentoId());
            ps.setString(4, os.getModeloEquipamento());
            ps.setString(5, os.getMotivo().name());
            ps.setString(6, os.getStatus().name());
            ps.setString(7, os.getDescricao());
            ps.setString(8, os.getDataAbertura().toString());
            ps.setString(9, os.getDepartamentoDestino());
            ps.setDouble(10, os.getUltimaTensao());
            ps.setString(11, os.getSituacaoRede());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                os.setId(rs.getInt(1));
                return os.getId();
            }
        }
        return -1;
    }

    private OrdemServico mapOrdemServico(ResultSet rs) throws SQLException {
        OrdemServico os = new OrdemServico();
        os.setId(rs.getInt("id"));
        os.setClienteId(rs.getInt("cliente_id"));
        os.setNomeCliente(rs.getString("nome_cliente"));
        os.setEquipamentoId(rs.getInt("equipamento_id"));
        os.setModeloEquipamento(rs.getString("modelo_equipamento"));
        os.setMotivo(OrdemServico.Motivo.valueOf(rs.getString("motivo")));
        os.setStatus(OrdemServico.Status.valueOf(rs.getString("status")));
        os.setDescricao(rs.getString("descricao"));
        String da = rs.getString("data_abertura");
        if (da != null) os.setDataAbertura(LocalDateTime.parse(da));
        os.setDepartamentoDestino(rs.getString("departamento_destino"));
        os.setUltimaTensao(rs.getDouble("ultima_tensao"));
        os.setSituacaoRede(rs.getString("situacao_rede"));
        return os;
    }

    public void fecharConexao() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

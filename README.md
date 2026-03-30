#G13

Alunos:
 Emiliano Rodrigues Feliciano,
 Elberty Borges de Oliveira e
 Gabriel Henrique Oliveira Silva.

INTRODUÇÃO
 Desde 2012, com a publicação da Resolução Normativa nº 482 pela ANEEL, que permitiu aos consumidores instalarem pequenos sistemas e compensarem o excedente de energia na rede, observamos um "boom" de instalações de geração de energia fotovoltaica. Normalmente o sistema é gerenciado com a visita de um técnico especializado, no local onde estiver operando. Para que o sistema não seja sobrecarregado ou fornecendo tensão abaixo do padrão (19V), é necessário o controle para que se mantenha dentro de limites pré-definidos. Neste projeto, gerenciaremos apenas a tensão no sistema simulado.

OBJETIVOS
 Com o objetivo de facilitar este monitoramento, será implementado software e hardware para medição de tensão da geração simulada e transmissão por wireless para central de processamento. Será considerada tensão dentro do padrão, caso a mesma se mantenha entre 18V (mínimo) e 20V (máximo).

EQUIPAMENTOS, SOFTWARES E MATERIAIS
1 - Fonte de tensão 19V,
1 - Placa ESP32,
1 - Roteador A930H,
1 - Computador,
- Cabos de transmissão e
- Software Eclipse.
  
CRONOGRAMA
16/03 - Definição do tema do projeto: Sistema de Monitoração de Tensão de Fonte de 19V, simulando placa fotovoltáica com ESP32 e envio de dados via Wi-Fi.
23/03 - Definição do cronograma.
30/03 - Estrutura base do programa: Configuração inicial no ESP32 e criação da base do código no Eclipse.
06/04 - Definições de objetos e classes. Definição das estruturas do sistema: Classe de leitura de tensão, classe de comunicação Wi-Fi, classe de envio de dados ao servidor.
13/04 - Aprimoramento das características dos objetos:  Ajuste da leitura e calibração da tensão. Melhoria na estrutura do código e organização das funções.
20/04 - Implementação da leitura de tensão: Leitura da fonte de 19V pelo ESP32 e conversão para valores reais. Testes no monitor.
27/04 - Desenvolvimento do envio de dados (Wi-Fi): Testes de comunicação. Conexão com roteador A930H e envio de dados via HTTP ou MQTT.
04/05 - Criação da lógica de monitoramento (simulação de falhas): Definir limites de tensão e detectar quedas ou valores anormais. Simular falhas na fonte.
11/05 - Testes de validação de fluxo e correção de bugs: Testar leitura→envio→recepção e corrigir erros de comunicação. Validar estabilidade.
18/05 - Formatação da saída de dados: Exibição no servidor (interface) e organização dos dados recebidos. Apresentação das tensões e alertas.
25/05 - Revisão final: Revisar todo o sistema, organizar código e slides para apresentação do funcionamento.
01/06 - Seminário.

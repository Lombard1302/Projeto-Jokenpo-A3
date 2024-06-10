import java.io.*;
import java.net.*;
import java.util.*;

public class Jogo {

    public static void main(String[] args) throws Exception {
        
            Scanner modo = new Scanner(System.in);

            System.out.println("Seja bem vindo ao menu do jogo JOKENPO!\n");

            System.out.println("Escolha o modo de jogo:\n ");
            System.out.println("1-> individual (contra máquina).\n ");
            System.out.println("2-> competitivo (contra jogador). \n\n");


        int escolha = modo.nextInt();


            switch (escolha) {

                case 1: 
                    iniciarIndividual(new BufferedReader(new InputStreamReader(System.in)), new PrintWriter(System.out, true));
                    break;

                case 2:
                    iniciarMultijogador(new BufferedReader(new InputStreamReader(System.in)), new PrintWriter(System.out, true));
                    break;

                default:
                    System.out.println("Modo inválido! Finalizando programa. ");
                    break;
        }
            modo.close();
    }

    //método jogo Individual :

    private static void iniciarIndividual(BufferedReader in, PrintWriter out) throws IOException{
        
            System.out.println("Você selecionou modo individual.\n");
            int vitorias = 0, derrotas = 0, empate = 0;
            Random maquina = new Random();
            String[] opcoes = {"pedra","papel","tesoura"};
    //Lógica jogo 

        while (true) {
            
            out.println("Escolha entre as seguintes opções: \n\n 1-> Pedra. \n 2-> Papel. \n 3-> Tesoura. \n\n Digite 'Sair' para abandonar o jogo");
            String escolha = in.readLine();

            if ("sair".equalsIgnoreCase(escolha)) {
                
                out.println("Você saiu do jogo. Obrigado pro jogar!");
                break;
            }

            int escolhaH = Integer.parseInt(escolha);
            int escolhaM = maquina.nextInt(3) +1;

            
            out.println("A maquina escolheu:   "+opcoes[escolhaM -1]);
            int resultado = vencedor(escolhaH, escolhaM);

            switch (resultado) {

                case 0:                
                out.print("\nA rodada deu empate.\n");
                empate++;
                break;
                
                case 1:
                out.println("\nVocê venceu. Parabéns.\n");
                    vitorias++;
                break;

                case 2:
                out.println("\nInfelizmente você perdeu a rodada...\n");
                derrotas++;
                break;

        }

            out.printf( " Você Venceu: %d vezes;\n Você Perdeu: %d vezes;\n Você Empatou: %d vezes.\n\n",vitorias,derrotas,empate);
            
        }
        in.close();
        out.close();
    
}
    

    private static void iniciarMultijogador(BufferedReader in, PrintWriter out) throws IOException {
            System.out.println("Você selecionou modo competitivo.");
    
            System.out.print("Escolha um endereço IP para o servidor (ou pressione Enter para usar localhost): ");
            String ipAddress = in.readLine();
        if (ipAddress.isEmpty()) {
            ipAddress = "localhost";
        }

            System.out.print("Escolha um número de porta para o servidor: ");
            int porta = Integer.parseInt(in.readLine());

        if (porta < 1024 || porta > 65535) {
            System.out.println("Número de porta inválido.");
            return;
        }

            ServerSocket serverSocket = new ServerSocket(porta);
            
            System.out.println("Servidor iniciado na porta " + porta + ". Aguardando conexões...");

    // LISTA DE AGUARDO
            List<Socket> waitingPlayers = new ArrayList<>();

        try{

        while (true) {
            Socket playerSocket = serverSocket.accept();
            System.out.println("Novo jogador conectado!");

    // JOGADOR NOVO A SER ADICIONADO NA LISTA
            waitingPlayers.add(playerSocket);

    // CONDICIONAL PARA CRIAR NOVO JOGO CASO TENHAM MAIS DE 2 JOGADORES
        if (waitingPlayers.size() >= 2) {
    // REMOVER JOGADORES DA FILA E INICIAR O JOGO PARA ELES 
            Socket player1Socket = waitingPlayers.remove(0);
            Socket player2Socket = waitingPlayers.remove(0);

            new GameThread(player1Socket, player2Socket).start();
        }
    }
        }catch (IOException e) {
            System.err.println("Erro ao jogar partida: "+ e.getMessage());
        } finally {
            serverSocket.close();
    }}
    private static class GameThread extends Thread {

            private Socket player1Socket;
            private Socket player2Socket;

        public GameThread(Socket player1Socket, Socket player2Socket) {
            this.player1Socket = player1Socket;
            this.player2Socket = player2Socket;
    }

    @Override
        public void run() {

        try {
            BufferedReader player1In = new BufferedReader(new InputStreamReader(player1Socket.getInputStream()));
            PrintWriter player1Out = new PrintWriter(player1Socket.getOutputStream(), true);

            BufferedReader player2In = new BufferedReader(new InputStreamReader(player2Socket.getInputStream()));
            PrintWriter player2Out = new PrintWriter(player2Socket.getOutputStream(), true);

        
            playGame(player1In, player1Out, player2In, player2Out);
        } catch (IOException e) {
            System.err.println("Erro ao jogar partida: " + e.getMessage());
        }
    }

    private void playGame(BufferedReader player1In, PrintWriter player1Out, BufferedReader player2In, PrintWriter player2Out) throws IOException {
        // PLACAR INICIAL 
        int player1Wins = 0;
        int player2Wins = 0;
        int draws = 0;

        while (true) {

            player1Out.println("Escolha entre as seguintes opções: \n\n 1-> Pedra. \n 2-> Papel. \n 3-> Tesoura.");
            String player1Move = player1In.readLine();

            player2Out.println("Escolha entre as seguintes opções: \n\n 1-> Pedra. \n 2-> Papel. \n 3-> Tesoura.");
            String player2Move = player2In.readLine();

            // VENCEDOR DA RODADA
            int result = vencedor(Integer.parseInt(player1Move), Integer.parseInt(player2Move));

            switch (result) {
                case 0:
                    draws++;
                    player1Out.println("Empate!");
                    player2Out.println("Empate!");
                    break;
                case 1:
                    player1Wins++;
                    player1Out.println("Você venceu!");
                    player2Out.println("Você perdeu!");
                    break;
                case 2:
                    player2Wins++;
                    player1Out.println("Você perdeu!");
                    player2Out.println("Você venceu!");
                    break;
            }

            // PRINT DO PLACAR ATUAL
            player1Out.printf("Placar: Você %d, Oponente %d, Empates %d.\n", player1Wins, player2Wins, draws);
            player2Out.printf("Placar: Você %d, Oponente %d, Empates %d.\n", player2Wins, player1Wins, draws);
        }
    }  
    }

        
    private static int vencedor(int escolhaH, int escolhaM) {
        if (escolhaH == escolhaM) {return 0;} // Empate
        if ((escolhaH == 1 && escolhaM == 3) || (escolhaH == 2 && escolhaM == 1) || (escolhaH == 3 && escolhaM == 2)){ return 1;} // Jogador ganha
        else { return 2; } // Máquina ganha
    }
}


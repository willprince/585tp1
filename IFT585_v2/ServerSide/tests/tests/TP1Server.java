package tests;

import app.Server;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;

public class TP1Server {

    /**
     * A)
     *  1. Client envoie un fichier de 40 Mo vers l'application serveur.
     *  2. Serveur envoie un fichier de 40 Mo vers l'application client.
     *
     *  partir le test A1 pour le serveur, puis le pour le client.
     */
    @Test
    public void startServerAndThreads() throws IOException {
        //init
        InetAddress iPAddress = InetAddress.getByName("localhost");
        int port = 99;

        Server server = new Server(port, iPAddress);
        server.start();
    }

    //TODO : faire des tests pour prouver que les exigences du tp1 sont chill

    //B) protocole udp

    //C) communication malgre debranchement occasionel

    //D) Go-Back-N ou Selective Repeat

    //E) Taille de fenetre a l'emmeteur doit etre de 4 paquets ou plus

    //F) La taille des paquets doit etre ajuste pour etre inferieur a 65515 octets

    //G) Le serveur doit pouvoir accepter plusieurs clients en meme temps

    //H) Affichage qui permet de suivre la transmission
}

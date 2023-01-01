package yesnogame;

import java.io.IOException;


public class Node {
    private String text;
    private Node yes;  // if null, it's an answer
    private Node no;

    public Node(String answer) {
        this.text = answer;
    }

    public Node(String question, Node yes, Node no) {
        this.text = question;
        this.yes = yes;
        this.no = no;
    }

    public void run () throws IOException {

        if (yes == null)
            System.out.println ("Answer: "+text);
        else {
            System.out.println (text+ " (y/n)");

            while (true) {
                int i = System.in.read();
                if (i == 'y' || i == 'Y') {
                    yes.run();
                    break;
                }
                else if (i == 'n' || i == 'N') {
                    no.run();
                    break;
                }
            }
        }
    }

}


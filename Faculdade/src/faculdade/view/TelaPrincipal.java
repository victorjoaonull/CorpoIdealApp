/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package faculdade.view;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

/**
 *
 * @author Victor
 */
public class TelaPrincipal extends JFrame {
    private int userId;
    private String nomeUsuario;
    
    public TelaPrincipal(int userId, String nomeUsuario) {
        this.userId = userId;
        this.nomeUsuario = nomeUsuario;
        
        setTitle("CorpoIdeal - " + nomeUsuario);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Cria abas
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Plano Nutricional", new PainelPlanoNutricional(userId));
        abas.addTab("Acompanhamento", new PainelAcompanhamento(userId));
        abas.addTab("Perfil", new PainelPerfil(userId));
        
        add(abas);
        
        // Menu superior
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArquivo = new JMenu("Arquivo");
        JMenuItem itemSair = new JMenuItem("Sair");
        
        itemSair.addActionListener(e -> {
            this.dispose();
            new TelaLogin().setVisible(true);
        });
        
        menuArquivo.add(itemSair);
        menuBar.add(menuArquivo);
        setJMenuBar(menuBar);
    }
}
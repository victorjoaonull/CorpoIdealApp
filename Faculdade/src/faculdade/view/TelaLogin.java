/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package faculdade.view;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import faculdade.connection.Database;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 *
 * @author Victor
 */
public class TelaLogin extends JFrame {
    private JTextField txtEmail;
    private JPasswordField txtSenha;
    
    public TelaLogin() {
        setTitle("CorpoIdeal - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Componentes
        JLabel lblTitulo = new JLabel("Bem-vindo ao CorpoIdeal");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        
        JLabel lblEmail = new JLabel("E-mail:");
        txtEmail = new JTextField(20);
        
        JLabel lblSenha = new JLabel("Senha:");
        txtSenha = new JPasswordField(20);
        
        JButton btnLogin = new JButton("Login");
        JButton btnCadastrar = new JButton("Cadastrar");
        
        // Layout
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblTitulo, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(lblEmail, gbc);
        
        gbc.gridx = 1;
        panel.add(txtEmail, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblSenha, gbc);
        
        gbc.gridx = 1;
        panel.add(txtSenha, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(btnLogin, gbc);
        
        gbc.gridx = 1;
        panel.add(btnCadastrar, gbc);
        
        // Ações
        btnLogin.addActionListener(e -> fazerLogin());
        btnCadastrar.addActionListener(e -> {
            this.dispose();
            new TelaCadastro().setVisible(true);
        });
        
        add(panel);
    }
    
    private void fazerLogin() {
        // Implementação da lógica de login
        String email = txtEmail.getText();
        String senha = new String(txtSenha.getPassword());
        
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, nome FROM usuarios WHERE email = ? AND senha = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, senha);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("id");
                String nome = rs.getString("nome");
                
                this.dispose();
                new TelaPrincipal(userId, nome).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "E-mail ou senha incorretos!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao fazer login: " + ex.getMessage());
        }
    }
}
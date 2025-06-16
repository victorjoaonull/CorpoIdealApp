package faculdade.view;

import faculdade.connection.Database;
import faculdade.model.Atualizavel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;

public class PainelPerfil extends JPanel {
    private int userId;
    private JLabel lblNome, lblEmail;
    private JComboBox<String> cbObjetivo, cbAtividade;
    private JButton btnSalvar;
    
    public PainelPerfil(int userId) {
        this.userId = userId;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Componentes
        lblNome = new JLabel();
        lblEmail = new JLabel();
        
        String[] objetivos = {"Perder peso", "Ganhar massa", "Manter peso"};
        cbObjetivo = new JComboBox<>(objetivos);
        
        String[] atividades = {"Sedentário", "Leve", "Moderado", "Intenso", "Muito intenso"};
        cbAtividade = new JComboBox<>(atividades);
        
        btnSalvar = new JButton("Salvar Alterações");
        
        // Layout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(new JLabel("Informações do Perfil", JLabel.CENTER), gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        add(new JLabel("Nome:"), gbc);
        
        gbc.gridx = 1;
        add(lblNome, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("E-mail:"), gbc);
        
        gbc.gridx = 1;
        add(lblEmail, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Objetivo:"), gbc);
        
        gbc.gridx = 1;
        add(cbObjetivo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("Nível de Atividade:"), gbc);
        
        gbc.gridx = 1;
        add(cbAtividade, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnSalvar, gbc);
        
        // Carregar dados
        carregarDados();
        
        // Ação do botão
        btnSalvar.addActionListener(e -> salvarAlteracoes());
    }
    
    private void carregarDados() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT nome, email, objetivo, nivel_atividade FROM usuarios WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                lblNome.setText(rs.getString("nome"));
                lblEmail.setText(rs.getString("email"));
                cbObjetivo.setSelectedItem(rs.getString("objetivo"));
                cbAtividade.setSelectedItem(rs.getString("nivel_atividade"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar perfil: " + ex.getMessage());
        }
    }
    
    private void salvarAlteracoes() {
        String novoObjetivo = (String) cbObjetivo.getSelectedItem();
        String novaAtividade = (String) cbAtividade.getSelectedItem();
        
        try (Connection conn = Database.getConnection()) {
            // Atualiza os dados do usuário
            String sqlUpdateUser = "UPDATE usuarios SET objetivo = ?, nivel_atividade = ? WHERE id = ?";
            PreparedStatement stmtUser = conn.prepareStatement(sqlUpdateUser);
            stmtUser.setString(1, novoObjetivo);
            stmtUser.setString(2, novaAtividade);
            stmtUser.setInt(3, userId);
            stmtUser.executeUpdate();
            
            // Obtém os dados necessários para recalcular o plano
            String sqlUserData = "SELECT peso, altura, genero FROM usuarios WHERE id = ?";
            PreparedStatement stmtData = conn.prepareStatement(sqlUserData);
            stmtData.setInt(1, userId);
            ResultSet rs = stmtData.executeQuery();
            
            if (rs.next()) {
                double peso = rs.getDouble("peso");
                double altura = rs.getDouble("altura");
                String genero = rs.getString("genero");
                
                // Cria um novo plano nutricional
                criarNovoPlanoNutricional(peso, altura, genero, novoObjetivo, novaAtividade);
                
                JOptionPane.showMessageDialog(this, "Perfil atualizado com sucesso!\n"
                    + "Seu plano nutricional foi recalculado.");
                
                // Notificar outras abas para atualizar
                notificarAtualizacao();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar perfil: " + ex.getMessage());
        }
    }
    
    private void criarNovoPlanoNutricional(double peso, double altura, String genero, 
                                         String objetivo, String nivelAtividade) throws SQLException {
        Connection conn = Database.getConnection();
        
        // Cálculo simplificado das necessidades nutricionais
        double tmb = genero.equals("Masculino") 
            ? 88.362 + (13.397 * peso) + (4.799 * altura * 100) - (5.677 * 25)
            : 447.593 + (9.247 * peso) + (3.098 * altura * 100) - (4.330 * 25);
        
        double fatorAtividade = switch (nivelAtividade) {
            case "Sedentário" -> 1.2;
            case "Leve" -> 1.375;
            case "Moderado" -> 1.55;
            case "Intenso" -> 1.725;
            case "Muito intenso" -> 1.9;
            default -> 1.2;
        };
        
        double calorias = tmb * fatorAtividade;
        
        // Ajuste conforme objetivo
        if (objetivo.equals("Perder peso")) calorias *= 0.85;
        if (objetivo.equals("Ganhar massa")) calorias *= 1.15;
        
        // Inserir novo plano nutricional
        String sql = "INSERT INTO planos_nutricionais " +
                    "(usuario_id, calorias_diarias, proteinas_diarias, carboidratos_diarias, " +
                    "gorduras_diarias, motivo_alteracao) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, userId);
        stmt.setInt(2, (int) calorias);
        stmt.setInt(3, (int) (peso * 2.2));
        stmt.setInt(4, (int) ((calorias * 0.5) / 4));
        stmt.setInt(5, (int) ((calorias * 0.3) / 9));
        stmt.setString(6, "Alteração de objetivo/atividade: " + objetivo + "/" + nivelAtividade);
        
        stmt.executeUpdate();
    }
    
    private void notificarAtualizacao() {
        // Obtém a janela principal
        Container parent = this.getParent();
        while (parent != null && !(parent instanceof JTabbedPane)) {
            parent = parent.getParent();
        }
        
        if (parent != null) {
            JTabbedPane abas = (JTabbedPane) parent;
            
            // Atualiza cada aba
            for (int i = 0; i < abas.getTabCount(); i++) {
                Component componente = abas.getComponentAt(i);
                if (componente instanceof Atualizavel) {
                    ((Atualizavel) componente).atualizarDados();
                }
            }
        }
    }
}
package faculdade.view;

import faculdade.connection.Database;
import faculdade.model.Atualizavel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PainelAcompanhamento extends JPanel implements Atualizavel {
    private int userId;
    private DefaultTableModel modelo;
    
    public PainelAcompanhamento(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        
        // Tabela de histórico
        String[] colunas = {"Data", "Peso (kg)", "Observações"};
        modelo = new DefaultTableModel(colunas, 0);
        JTable tabelaHistorico = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tabelaHistorico);
        
        // Painel inferior
        JPanel painelInferior = new JPanel();
        JButton btnRegistrarPeso = new JButton("Registrar Peso Atual");
        JButton btnAtualizar = new JButton("Atualizar Histórico");
        
        painelInferior.add(btnRegistrarPeso);
        painelInferior.add(btnAtualizar);
        
        // Layout
        add(new JLabel("Histórico de Acompanhamento", JLabel.CENTER), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);
        
        // Carregar dados
        carregarHistorico();
        
        // Ações
        btnRegistrarPeso.addActionListener(e -> registrarPeso());
        btnAtualizar.addActionListener(e -> carregarHistorico());
    }
    
    private void carregarHistorico() {
        try (Connection conn = Database.getConnection()) {
            modelo.setRowCount(0); // Limpa a tabela
            
            String sql = "SELECT data_registro, peso, observacoes FROM acompanhamento " +
                         "WHERE usuario_id = ? ORDER BY data_registro DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    sdf.format(rs.getDate("data_registro")),
                    rs.getDouble("peso"),
                    rs.getString("observacoes") != null ? rs.getString("observacoes") : ""
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar histórico: " + ex.getMessage());
        }
    }
    
    private void registrarPeso() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField txtPeso = new JTextField();
        JTextArea txtObservacoes = new JTextArea(3, 20);
        
        panel.add(new JLabel("Peso (kg):"));
        panel.add(txtPeso);
        panel.add(new JLabel("Observações:"));
        panel.add(new JScrollPane(txtObservacoes));
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Registrar Peso", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                double peso = Double.parseDouble(txtPeso.getText());
                String observacoes = txtObservacoes.getText();
                
                try (Connection conn = Database.getConnection()) {
                    String sql = "INSERT INTO acompanhamento (usuario_id, peso, observacoes) VALUES (?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, userId);
                    stmt.setDouble(2, peso);
                    stmt.setString(3, observacoes.isEmpty() ? null : observacoes);
                    stmt.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this, "Peso registrado com sucesso!");
                    carregarHistorico();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Erro ao registrar peso: " + ex.getMessage());
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, insira um valor válido para o peso.");
            }
        }
    }
    
    @Override
    public void atualizarDados() {
        carregarHistorico();
    }
}
package faculdade.view;

import faculdade.connection.Database;
import faculdade.model.Atualizavel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

public class PainelPlanoNutricional extends JPanel implements Atualizavel {
    private int userId;
    private JComboBox<String> cbHistPlanos;
    private DefaultTableModel modelo;
    private JLabel lblCalorias, lblProteinas, lblCarboidratos, lblGorduras;
    private int planoAtualId = -1;
    
    public PainelPlanoNutricional(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        
        // Painel superior com resumo e histórico
        JPanel painelSuperior = new JPanel(new BorderLayout());
        
        // Painel de resumo nutricional
        JPanel painelResumo = criarPainelResumo();
        
        // Combo box para histórico de planos
        cbHistPlanos = new JComboBox<>();
        JPanel painelHistorico = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelHistorico.add(new JLabel("Histórico de Planos:"));
        painelHistorico.add(cbHistPlanos);
        
        painelSuperior.add(painelResumo, BorderLayout.CENTER);
        painelSuperior.add(painelHistorico, BorderLayout.SOUTH);
        
        // Tabela de refeições
        String[] colunas = {"Refeição", "Descrição", "Calorias (kcal)", "Proteínas (g)", "Carboidratos (g)", "Gorduras (g)"};
        modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: case 1: return String.class;
                    case 2: case 3: case 4: case 5: return Integer.class;
                    default: return Object.class;
                }
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable tabelaRefeicoes = new JTable(modelo);
        tabelaRefeicoes.setRowHeight(30);
        tabelaRefeicoes.setAutoCreateRowSorter(true);
        
        // Centraliza conteúdo das células
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tabelaRefeicoes.getColumnCount(); i++) {
            tabelaRefeicoes.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(tabelaRefeicoes);
        
        // Painel de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnAdicionarRefeicao = new JButton("Adicionar Refeição");
        JButton btnAtualizar = new JButton("Atualizar Plano");
        JButton btnDefinirAtual = new JButton("Definir como Plano Atual");
        
        painelBotoes.add(btnAdicionarRefeicao);
        painelBotoes.add(btnAtualizar);
        painelBotoes.add(btnDefinirAtual);
        
        // Layout
        add(painelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(painelBotoes, BorderLayout.SOUTH);
        
        // Carregar dados
        carregarDados();
        
        // Ações dos botões
        btnAtualizar.addActionListener(e -> {
    // Força uma limpeza completa antes de recarregar
    modelo.setRowCount(0);
    cbHistPlanos.removeAllItems();
    carregarDados();
});
        btnAdicionarRefeicao.addActionListener(e -> adicionarRefeicao());
        btnDefinirAtual.addActionListener(e -> definirPlanoAtual());
        
        // Ação do combo box
        cbHistPlanos.addActionListener(e -> carregarDadosPlanoSelecionado());
    }
    
    private JPanel criarPainelResumo() {
        JPanel painel = new JPanel(new GridLayout(1, 4, 10, 10));
        painel.setBorder(BorderFactory.createTitledBorder("Resumo Nutricional Diário"));
        painel.setBackground(new Color(240, 240, 240));
        
        lblCalorias = criarLabelNutricional("Calorias: --");
        lblProteinas = criarLabelNutricional("Proteínas: --");
        lblCarboidratos = criarLabelNutricional("Carboidratos: --");
        lblGorduras = criarLabelNutricional("Gorduras: --");
        
        painel.add(lblCalorias);
        painel.add(lblProteinas);
        painel.add(lblCarboidratos);
        painel.add(lblGorduras);
        
        return painel;
    }
    
    private JLabel criarLabelNutricional(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }
    
private void carregarDados() {
    // Limpa completamente antes de recarregar
    modelo.setRowCount(0);
    cbHistPlanos.removeAllItems();
    
    try (Connection conn = Database.getConnection()) {
        // 1. Obter plano atual
        planoAtualId = -1;
        String sqlPlanoAtual = "SELECT id FROM planos_nutricionais WHERE usuario_id = ? AND atual = TRUE";
        try (PreparedStatement stmt = conn.prepareStatement(sqlPlanoAtual)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                planoAtualId = rs.getInt("id");
            }
        }

        // 2. Carregar histórico de planos
        String sqlPlanos = "SELECT id, data_criacao FROM planos_nutricionais " +
                         "WHERE usuario_id = ? ORDER BY data_criacao DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sqlPlanos)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            while (rs.next()) {
                int planoId = rs.getInt("id");
                String texto = "Plano de " + sdf.format(rs.getDate("data_criacao"));
                if (planoId == planoAtualId) {
                    texto += " (Atual)";
                }
                cbHistPlanos.addItem(texto);
            }
        }

        // 3. Carregar o plano mais recente
        if (cbHistPlanos.getItemCount() > 0) {
            carregarDadosPlanoSelecionado();
        }
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, 
            "Erro ao carregar dados: " + ex.getMessage());
        ex.printStackTrace();
    }
}
    
private void carregarDadosPlanoSelecionado() {
    int selectedIndex = cbHistPlanos.getSelectedIndex();
    if (selectedIndex == -1) return;

    // Limpa completamente
    modelo.setRowCount(0);
    
    try (Connection conn = Database.getConnection()) {
        // 1. Obter ID do plano selecionado
        String sqlPlanoId = "SELECT id FROM planos_nutricionais " +
                          "WHERE usuario_id = ? ORDER BY data_criacao DESC LIMIT 1 OFFSET ?";
        int planoId;
        try (PreparedStatement stmt = conn.prepareStatement(sqlPlanoId)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, selectedIndex);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return;
            planoId = rs.getInt("id");
        }

        // 2. Carregar dados do plano
        String sqlPlano = "SELECT * FROM planos_nutricionais WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlPlano)) {
            stmt.setInt(1, planoId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                atualizarResumoNutricional(
                    rs.getInt("calorias_diarias"),
                    rs.getInt("proteinas_diarias"),
                    rs.getInt("carboidratos_diarias"),
                    rs.getInt("gorduras_diarias")
                );
            }
        }

        // 3. Carregar refeições
        String sqlRefeicoes = "SELECT * FROM refeicoes WHERE plano_id = ? ORDER BY FIELD(tipo, " +
                            "'Café da manhã', 'Lanche da manhã', 'Almoço', 'Lanche da tarde', 'Jantar', 'Ceia')";
        try (PreparedStatement stmt = conn.prepareStatement(sqlRefeicoes)) {
            stmt.setInt(1, planoId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getString("tipo"),
                    rs.getString("descricao"),
                    rs.getInt("calorias"),
                    rs.getInt("proteinas"),
                    rs.getInt("carboidratos"),
                    rs.getInt("gorduras")
                });
            }
        }

        // 4. Adicionar totais (apenas uma vez)
        adicionarTotais();
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, 
            "Erro ao carregar plano selecionado: " + ex.getMessage());
        ex.printStackTrace();
    }
}
    
    private void carregarRefeicoes(int planoId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String sqlRefeicoes = "SELECT * FROM refeicoes WHERE plano_id = ? ORDER BY FIELD(tipo, " +
                                 "'Café da manhã', 'Lanche da manhã', 'Almoço', 'Lanche da tarde', 'Jantar', 'Ceia')";
            PreparedStatement stmtRefeicoes = conn.prepareStatement(sqlRefeicoes);
            stmtRefeicoes.setInt(1, planoId);
            ResultSet rsRefeicoes = stmtRefeicoes.executeQuery();
            
            while (rsRefeicoes.next()) {
                modelo.addRow(new Object[]{
                    rsRefeicoes.getString("tipo"),
                    rsRefeicoes.getString("descricao"),
                    rsRefeicoes.getInt("calorias"),
                    rsRefeicoes.getInt("proteinas"),
                    rsRefeicoes.getInt("carboidratos"),
                    rsRefeicoes.getInt("gorduras")
                });
            }
        }
    }
    
    private void atualizarResumoNutricional(int calorias, int proteinas, int carboidratos, int gorduras) {
        lblCalorias.setText(String.format("<html><b>Calorias:</b><br>%d kcal</html>", calorias));
        lblProteinas.setText(String.format("<html><b>Proteínas:</b><br>%d g</html>", proteinas));
        lblCarboidratos.setText(String.format("<html><b>Carboidratos:</b><br>%d g</html>", carboidratos));
        lblGorduras.setText(String.format("<html><b>Gorduras:</b><br>%d g</html>", gorduras));
    }
    
    private void adicionarTotais() {
        removeLinhasDeTotais();
        int totalCalorias = 0;
        int totalProteinas = 0;
        int totalCarboidratos = 0;
        int totalGorduras = 0;

        for (int i = 0; i < modelo.getRowCount(); i++) {
            totalCalorias += parseIntSafe(modelo.getValueAt(i, 2));
            totalProteinas += parseIntSafe(modelo.getValueAt(i, 3));
            totalCarboidratos += parseIntSafe(modelo.getValueAt(i, 4));
            totalGorduras += parseIntSafe(modelo.getValueAt(i, 5));
        }

        // Adiciona linha de totais
        modelo.addRow(new Object[]{
            "<html><b>TOTAL CONSUMIDO</b></html>",
            "",
            totalCalorias,
            totalProteinas,
            totalCarboidratos,
            totalGorduras
        });

        // Adiciona linha de metas (se disponível)
        try {
            int metaCalorias = parseIntSafe(lblCalorias.getText());
            int metaProteinas = parseIntSafe(lblProteinas.getText());
            int metaCarboidratos = parseIntSafe(lblCarboidratos.getText());
            int metaGorduras = parseIntSafe(lblGorduras.getText());

            modelo.addRow(new Object[]{
                "<html><b>META DIÁRIA</b></html>",
                "",
                metaCalorias,
                metaProteinas,
                metaCarboidratos,
                metaGorduras
            });

            // Adiciona linha de diferença
            modelo.addRow(new Object[]{
                "<html><b>DIFERENÇA</b></html>",
                "",
                (totalCalorias - metaCalorias),
                (totalProteinas - metaProteinas),
                (totalCarboidratos - metaCarboidratos),
                (totalGorduras - metaGorduras)
            });
        } catch (NumberFormatException e) {
            System.err.println("Erro ao parsear metas: " + e.getMessage());
        }
    }

    private int parseIntSafe(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        try {
            String str = value.toString().replaceAll("<[^>]+>", "").replaceAll("[^0-9-]", "");
            return str.isEmpty() ? 0 : Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private void adicionarRefeicao() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Adicionar Nova Refeição");
        dialog.setModal(true);
        dialog.setSize(400, 400);
        dialog.setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        // Campos do formulário
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{
            "Café da manhã", "Lanche da manhã", "Almoço", "Lanche da tarde", "Jantar", "Ceia"
        });
        
        JTextArea taDescricao = new JTextArea(3, 20);
        JScrollPane scrollDescricao = new JScrollPane(taDescricao);
        
        JTextField txtCalorias = new JTextField();
        JTextField txtProteinas = new JTextField();
        JTextField txtCarboidratos = new JTextField();
        JTextField txtGorduras = new JTextField();
        
        // Adiciona componentes ao painel
        formPanel.add(new JLabel("Tipo de Refeição:"));
        formPanel.add(cbTipo);
        formPanel.add(new JLabel("Descrição:"));
        formPanel.add(scrollDescricao);
        formPanel.add(new JLabel("Calorias (kcal):"));
        formPanel.add(txtCalorias);
        formPanel.add(new JLabel("Proteínas (g):"));
        formPanel.add(txtProteinas);
        formPanel.add(new JLabel("Carboidratos (g):"));
        formPanel.add(txtCarboidratos);
        formPanel.add(new JLabel("Gorduras (g):"));
        formPanel.add(txtGorduras);
        
        // Painel de botões
        JPanel buttonPanel = new JPanel();
        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        
        buttonPanel.add(btnSalvar);
        buttonPanel.add(btnCancelar);
        
        // Adiciona ao dialog
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Ações dos botões
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        btnSalvar.addActionListener(e -> {
            try {
                String tipo = (String) cbTipo.getSelectedItem();
                String descricao = taDescricao.getText();
                int calorias = Integer.parseInt(txtCalorias.getText());
                int proteinas = Integer.parseInt(txtProteinas.getText());
                int carboidratos = Integer.parseInt(txtCarboidratos.getText());
                int gorduras = Integer.parseInt(txtGorduras.getText());
                
                if (descricao.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Por favor, insira uma descrição para a refeição.");
                    return;
                }
                
                salvarRefeicao(tipo, descricao, calorias, proteinas, carboidratos, gorduras);
                dialog.dispose();
                carregarDados();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Por favor, insira valores numéricos válidos para os nutrientes.");
            }
        });
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void salvarRefeicao(String tipo, String descricao, int calorias, int proteinas, int carboidratos, int gorduras) {
        try (Connection conn = Database.getConnection()) {
            // Obtém o ID do plano atual
            String sqlPlano = "SELECT id FROM planos_nutricionais WHERE usuario_id = ? AND atual = TRUE";
            PreparedStatement stmtPlano = conn.prepareStatement(sqlPlano);
            stmtPlano.setInt(1, userId);
            ResultSet rsPlano = stmtPlano.executeQuery();
            
            if (rsPlano.next()) {
                int planoId = rsPlano.getInt("id");
                
                // Insere a nova refeição
                String sql = "INSERT INTO refeicoes (plano_id, tipo, descricao, calorias, proteinas, carboidratos, gorduras) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, planoId);
                stmt.setString(2, tipo);
                stmt.setString(3, descricao);
                stmt.setInt(4, calorias);
                stmt.setInt(5, proteinas);
                stmt.setInt(6, carboidratos);
                stmt.setInt(7, gorduras);
                
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Refeição adicionada com sucesso!");
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum plano nutricional ativo encontrado para adicionar refeição.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar refeição: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void definirPlanoAtual() {
        try {
            if (cbHistPlanos.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this, "Nenhum plano selecionado!");
                return;
            }
            
            try (Connection conn = Database.getConnection()) {
                // Primeiro, obtém o ID do plano selecionado
                int selectedIndex = cbHistPlanos.getSelectedIndex();
                
                String sqlPlano = "SELECT id FROM planos_nutricionais " +
                                 "WHERE usuario_id = ? " +
                                 "ORDER BY data_criacao DESC " +
                                 "LIMIT 1 OFFSET ?";
                
                PreparedStatement stmtPlano = conn.prepareStatement(sqlPlano);
                stmtPlano.setInt(1, userId);
                stmtPlano.setInt(2, selectedIndex);
                ResultSet rsPlano = stmtPlano.executeQuery();
                
                if (rsPlano.next()) {
                    int novoPlanoId = rsPlano.getInt("id");
                    
                    // Primeiro, remove o status "atual" de todos os planos
                    String sqlResetAtual = "UPDATE planos_nutricionais SET atual = FALSE WHERE usuario_id = ?";
                    PreparedStatement stmtReset = conn.prepareStatement(sqlResetAtual);
                    stmtReset.setInt(1, userId);
                    stmtReset.executeUpdate();
                    
                    // Depois, define o novo plano como atual
                    String sqlSetAtual = "UPDATE planos_nutricionais SET atual = TRUE WHERE id = ?";
                    PreparedStatement stmtSet = conn.prepareStatement(sqlSetAtual);
                    stmtSet.setInt(1, novoPlanoId);
                    stmtSet.executeUpdate();
                    
                    planoAtualId = novoPlanoId;
                    
                    JOptionPane.showMessageDialog(this, "Plano definido como atual com sucesso!");
                    carregarDados(); // Recarrega para atualizar a exibição
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao definir plano atual: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void removeLinhasDeTotais() {
        for (int i = modelo.getRowCount() - 1; i >= 0; i--) {
            Object valor = modelo.getValueAt(i, 0);
            if (valor != null && (valor.toString().contains("TOTAL") || 
                valor.toString().contains("META") || 
                valor.toString().contains("DIFERENÇA"))) {
                modelo.removeRow(i);
            }
        }
    }
    
    @Override
    public void atualizarDados() {
        carregarDados();
        JOptionPane.showMessageDialog(this, "Plano nutricional atualizado com sucesso!");
    }
}
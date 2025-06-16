package faculdade.view;

import faculdade.connection.Database;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TelaCadastro extends JFrame {
    // Declaração dos componentes
    private JTextField txtNome, txtEmail, txtTelefone, txtDataNasc, txtAltura, txtPeso;
    private JPasswordField txtSenha;
    private JComboBox<String> cbGenero, cbObjetivo, cbAtividade;

    public TelaCadastro() {
        setTitle("CorpoIdeal - Cadastro");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Inicialização dos componentes ANTES de usá-los
        txtNome = new JTextField(20);
        txtEmail = new JTextField(20);
        txtSenha = new JPasswordField(20);
        txtTelefone = new JTextField(20);
        txtDataNasc = new JTextField(20);
        txtAltura = new JTextField(20);
        txtPeso = new JTextField(20);
        
        String[] generos = {"Masculino", "Feminino", "Outro"};
        cbGenero = new JComboBox<>(generos);
        
        String[] objetivos = {"Perder peso", "Ganhar massa", "Manter peso"};
        cbObjetivo = new JComboBox<>(objetivos);
        
        String[] atividades = {"Sedentário", "Leve", "Moderado", "Intenso", "Muito intenso"};
        cbAtividade = new JComboBox<>(atividades);
        
        // Configuração do layout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Adicionando componentes ao painel
        adicionarCampo(panel, gbc, 0, "Nome:", txtNome);
        adicionarCampo(panel, gbc, 1, "E-mail:", txtEmail);
        adicionarCampo(panel, gbc, 2, "Senha:", txtSenha);
        adicionarCampo(panel, gbc, 3, "Telefone:", txtTelefone);
        adicionarCampo(panel, gbc, 4, "Data Nasc. (DD/MM/AAAA):", txtDataNasc);
        adicionarCampo(panel, gbc, 5, "Gênero:", cbGenero);
        adicionarCampo(panel, gbc, 6, "Altura (m):", txtAltura);
        adicionarCampo(panel, gbc, 7, "Peso (kg):", txtPeso);
        adicionarCampo(panel, gbc, 8, "Objetivo:", cbObjetivo);
        adicionarCampo(panel, gbc, 9, "Nível Atividade:", cbAtividade);
        
        // Botões
        JButton btnCadastrar = new JButton("Cadastrar");
        JButton btnCancelar = new JButton("Cancelar");
        
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnCadastrar, gbc);
        
        gbc.gridx = 2;
        panel.add(btnCancelar, gbc);
        
        // Listeners
        btnCadastrar.addActionListener(this::cadastrarUsuario);
        btnCancelar.addActionListener(e -> {
            this.dispose();
            new TelaLogin().setVisible(true);
        });
        
        add(panel);
    }
    
    private void adicionarCampo(JPanel panel, GridBagConstraints gbc, int linha, String label, JComponent componente) {
        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(componente, gbc);
    }
    
    private void cadastrarUsuario(ActionEvent e) {
        try {
            // Validação básica
            if (txtNome.getText().trim().isEmpty() || 
                txtEmail.getText().trim().isEmpty() || 
                new String(txtSenha.getPassword()).trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome, e-mail e senha são obrigatórios!");
                return;
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date dataNasc = sdf.parse(txtDataNasc.getText());
            double altura = Double.parseDouble(txtAltura.getText());
            double peso = Double.parseDouble(txtPeso.getText());
            
            try (Connection conn = Database.getConnection()) {
                String sql = "INSERT INTO usuarios (nome, email, senha, telefone, data_nascimento, genero, altura, peso, objetivo, nivel_atividade) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, txtNome.getText());
                stmt.setString(2, txtEmail.getText());
                stmt.setString(3, new String(txtSenha.getPassword()));
                stmt.setString(4, txtTelefone.getText().isEmpty() ? null : txtTelefone.getText());
                stmt.setDate(5, new java.sql.Date(dataNasc.getTime()));
                stmt.setString(6, (String) cbGenero.getSelectedItem());
                stmt.setDouble(7, altura);
                stmt.setDouble(8, peso);
                stmt.setString(9, (String) cbObjetivo.getSelectedItem());
                stmt.setString(10, (String) cbAtividade.getSelectedItem());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows > 0) {
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        int userId = rs.getInt(1);
                        JOptionPane.showMessageDialog(this, "Cadastro realizado com sucesso!");
                        criarPlanoNutricional(userId, peso, altura, 
                            (String) cbGenero.getSelectedItem(),
                            (String) cbObjetivo.getSelectedItem(),
                            (String) cbAtividade.getSelectedItem());
                        this.dispose();
                        new TelaPrincipal(userId, txtNome.getText()).setVisible(true);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao cadastrar: " + ex.getMessage());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro nos dados: " + ex.getMessage());
        }
    }
    
    private void criarPlanoNutricional(int userId, double peso, double altura, 
                                     String genero, String objetivo, String nivelAtividade) {
        try (Connection conn = Database.getConnection()) {
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
            
            // Inserir plano nutricional
            String sql = "INSERT INTO planos_nutricionais " +
                        "(usuario_id, calorias_diarias, proteinas_diarias, carboidratos_diarias, gorduras_diarias) " +
                        "VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, (int) calorias);
            stmt.setInt(3, (int) (peso * 2.2)); // 2.2g de proteína por kg
            stmt.setInt(4, (int) ((calorias * 0.5) / 4)); // 50% de carboidratos
            stmt.setInt(5, (int) ((calorias * 0.3) / 9)); // 30% de gorduras
            
            stmt.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao criar plano nutricional: " + ex.getMessage());
        }
    }
}
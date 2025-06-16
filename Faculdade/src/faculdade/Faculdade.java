package faculdade;

/**
 *
 * @author Victor
 */
import faculdade.connection.Database;
import faculdade.view.TelaLogin;
import javax.swing.*;


public class Faculdade {
    public static void main(String[] args) {
        if (!Database.testarConexao()) {
    JOptionPane.showMessageDialog(null, 
        "Não foi possível conectar ao banco de dados.\n"
        + "Verifique se o MySQL está rodando e as credenciais estão corretas.",
        "Erro de Conexão",
        JOptionPane.ERROR_MESSAGE);
    System.exit(1);
}
        SwingUtilities.invokeLater(() -> {
            new TelaLogin().setVisible(true);
        });
        
    }
}
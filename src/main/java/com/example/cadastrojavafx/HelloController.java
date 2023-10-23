package com.example.cadastrojavafx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Utilizador;

import java.sql.*;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

public class HelloController {

   @FXML
   private TextField nome;

   @FXML
   private TextField idade;

    @FXML
    private TableView tableView;
    @FXML
    private TableColumn tableColumnnome;
    @FXML
    private TableColumn tableColumnidade;

    private List<Utilizador> utilizadors=new ArrayList<>();

    private Connection con=null;



    public void BaseDados() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/cadastro_utilitarios?characterEncoding=latin1", "root", "basedadosju");

        // Desativar o modo de auto commit
        con.setAutoCommit(false);
    }

    public void initialize() {
        try {
            BaseDados(); // Connect to the database
            loadTableData(); // Retrieve and load data into the TableView

            // Set up CellValueFactory for the TableView columns
            tableColumnnome.setCellValueFactory(new PropertyValueFactory<>("nome"));
            tableColumnidade.setCellValueFactory(new PropertyValueFactory<>("idade"));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadTableData() {
        // Clear existing data
        utilizadors.clear();
        tableView.getItems().clear();

        try {
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM utilizadors");

            while (resultSet.next()) {
                String nome = resultSet.getString("nome");
                int idade = resultSet.getInt("idade");

                Utilizador utilizador = new Utilizador(nome, idade);
                utilizadors.add(utilizador);
            }

            // Set data to the TableView
            ObservableList<Utilizador> data = FXCollections.observableArrayList(utilizadors);
            tableView.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    protected void onClickCadastrarUtilizador() throws SQLException, ClassNotFoundException, NumberFormatException  {

        try {
            BaseDados();
            String nome = this.nome.getText();
            Integer idade = Integer.valueOf(this.idade.getText());

            //INSERIR NO SQL
            PreparedStatement inserirAluno = con.prepareStatement("INSERT INTO utilizadors VALUES(?,?)");
            inserirAluno.setString(1, nome);
            inserirAluno.setInt(2, idade);
            inserirAluno.executeUpdate();

            Utilizador utilizador = new Utilizador(nome, idade);
            utilizadors.add(utilizador);

            //JAVAFX
            tableColumnnome.setCellValueFactory(new PropertyValueFactory<Utilizador, String>("nome"));
            tableColumnidade.setCellValueFactory(new PropertyValueFactory<Utilizador, Integer>("idade"));
            ObservableList<Utilizador> data = FXCollections.observableArrayList(utilizadors);
            tableView.setItems(data);

            con.commit();

            System.out.println("Utilizador: " + utilizador);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
        try {
            // Em caso de erro, desfazer as alterações
            con.rollback();
        } catch (SQLException rollbackException) {
            rollbackException.printStackTrace();
        }
        // Limpar os campos de texto após o cadastro
        this.nome.clear();
        this.idade.clear();
    }
    @FXML
    protected void onTableViewClicked() {

        Utilizador selectedUtilizador = (Utilizador) tableView.getSelectionModel().getSelectedItem();
        if (selectedUtilizador != null) {
            this.nome.setText(selectedUtilizador.getNome());
           this.idade.setText(String.valueOf(selectedUtilizador.getIdade()));
        }
    }

    @FXML
    protected void onEditar() { // Os nomes teem que ser compostos se nao altera tudo de uma vez...
        try {
            BaseDados();

            // Verificar se um usuário foi selecionado na tabela
            Utilizador selectedUtilizador = (Utilizador) tableView.getSelectionModel().getSelectedItem();

            String alterar=selectedUtilizador.getNome(); // foi preciso criar essas variavel pq a leitura do sql nao interpretou o o comando select

            if (selectedUtilizador != null) {
                // Obter os novos valores do TextField
                String novoNome = this.nome.getText();
                int novaIdade = Integer.parseInt(this.idade.getText());

                // Atualizar os valores do item selecionado
                selectedUtilizador.setNome(novoNome);
                selectedUtilizador.setIdade(novaIdade);

                // Atualizar no SQL
                PreparedStatement editarUsuario = con.prepareStatement("UPDATE utilizadors SET nome = ?, idade = ? WHERE nome = ?");
                editarUsuario.setString(1, novoNome);
                editarUsuario.setInt(2, novaIdade);
                editarUsuario.setString(3, alterar);
                editarUsuario.executeUpdate();

                // Atualizar a TableView
                tableView.refresh();

                // Efetivar as alterações no banco de dados
                con.commit();
        }
            try {
                // Em caso de erro, desfazer as alterações
                con.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }

    }
    @FXML
    protected void onApagar() throws SQLException {

        Utilizador selectedUtilizador = (Utilizador) tableView.getSelectionModel().getSelectedItem();

        String apagar=selectedUtilizador.getNome();

        if (selectedUtilizador != null) {


            PreparedStatement apagarUsuario=con.prepareStatement("DELETE FROM utilizadors WHERE nome = ?");
            apagarUsuario.setString(1, apagar);
            apagarUsuario.executeUpdate();

            utilizadors.remove(selectedUtilizador);
            tableView.getItems().remove(selectedUtilizador);
            // Efetivar as alterações no banco de dados
            con.commit();
        }


    }
    @FXML
    protected void onPesquisar() {
        String termoPesquisa = nome.getText().toLowerCase();

        ObservableList<Utilizador> resultadosPesquisa = FXCollections.observableArrayList();

        for (Utilizador utilizador : utilizadors) {
            String nome = utilizador.getNome().toLowerCase();

            if (nome.contains(termoPesquisa)) {
                resultadosPesquisa.add(utilizador);
            }
        }
        this.idade.clear();
        tableView.setItems(resultadosPesquisa);
    }
    @FXML
    protected void limparPesquisa() {
        nome.clear();
        idade.clear();
        tableView.setItems(FXCollections.observableArrayList(utilizadors));
    }
    @FXML
    public void apagarTodos() throws SQLException, ClassNotFoundException {
        BaseDados();

        utilizadors.clear();
        nome.clear();
        idade.clear();

        PreparedStatement apagarTodos=con.prepareStatement("DELETE FROM utilizadors");
        apagarTodos.executeUpdate();

        tableView.refresh();
        tableView.setItems(FXCollections.observableArrayList(utilizadors));

        // Efetivar as alterações no banco de dados
        con.commit();

    }
}
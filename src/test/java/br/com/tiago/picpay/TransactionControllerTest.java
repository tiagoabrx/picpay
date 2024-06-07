package br.com.tiago.picpay;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;


import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import br.com.tiago.picpay.transaction.Transaction;
import br.com.tiago.picpay.transaction.TransactionController;
import br.com.tiago.picpay.transaction.TransactionService;

@WebMvcTest(TransactionController.class)
@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    public void testCreateTransaction() throws Exception {
        // Criar uma instância válida de Transaction para teste
        Transaction transaction = new Transaction(1L, 2L, 3L, BigDecimal.TEN, LocalDateTime.now());

        // Preparação do mock para o serviço retornar a transação criada
        when(transactionService.create(transaction)).thenReturn(transaction);

        // Execução da requisição POST para criar uma transação
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/transaction")
                .contentType("application/json")
                .content("{}")) // JSON vazio como exemplo
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists()); // Verificar se o ID está presente no JSON retornado
    }

    @Test
    public void testListTransactions() throws Exception {
        // Criar uma instância válida de Transaction para teste
        Transaction transaction = new Transaction(1L, 2L, 3L, BigDecimal.TEN, LocalDateTime.now());

        // Preparação do mock para o serviço retornar uma lista contendo a transação criada
        when(transactionService.list()).thenReturn(Collections.singletonList(transaction));

        // Execução da requisição GET para listar as transações
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/transaction"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").exists()); // Verificar se o ID está presente no JSON retornado
    }
}
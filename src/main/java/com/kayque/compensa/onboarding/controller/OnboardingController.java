package com.kayque.compensa.onboarding.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class OnboardingController {

    private static final OnboardingPage[] PAGES = {
            new OnboardingPage(
                    "BEM-VINDO",
                    "Decida melhor antes de comprar",
                    "O Compensa? transforma o preço de uma compra em informações que ajudam você a decidir com mais consciência.",
                    "Veja quanto tempo de trabalho uma compra representa.",
                    "Considere planejamento, urgência, alternativas e motivação.",
                    "A decisão final continua sempre sendo sua."
            ),

            new OnboardingPage(
                    "SEU DINHEIRO",
                    "Configure seu perfil financeiro",
                    "Informe sua renda, suas horas de trabalho e seus compromissos mensais para receber cálculos próximos da sua realidade.",
                    "Descubra o valor da sua hora profissional.",
                    "Considere também o tempo adicional comprometido.",
                    "Acompanhe quanto realmente está disponível para escolhas."
            ),

            new OnboardingPage(
                    "ANTES DE COMPRAR",
                    "Analise cada decisão",
                    "Digite o produto, o preço e o contexto da compra. O Compensa? mostrará o impacto em dinheiro, tempo e orçamento.",
                    "Compare o preço com suas horas de trabalho.",
                    "Visualize o impacto mensal e a projeção anual.",
                    "Receba uma recomendação adaptada ao seu perfil."
            ),

            new OnboardingPage(
                    "SUA EVOLUÇÃO",
                    "Acompanhe decisões e padrões",
                    "Use as telas Hoje, Histórico, Insights e Relatório mensal para entender como suas escolhas estão evoluindo.",
                    "Reveja compras realizadas, evitadas ou em espera.",
                    "Descubra padrões por trás das suas decisões.",
                    "Compare seus resultados ao longo dos meses."
            ),

            new OnboardingPage(
                    "SUA CONQUISTA",
                    "Transforme escolhas em objetivos",
                    "Defina algo que deseja conquistar e acompanhe cada contribuição. O aplicativo mostrará quanto uma compra representa para essa meta.",
                    "Crie um objetivo financeiro pessoal.",
                    "Registre valores guardados separadamente.",
                    "Acompanhe marcos até chegar aos 100%."
            )
    };

    private int currentPageIndex;
    private Runnable onFinished;

    @FXML
    private Label stepLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label firstDetailLabel;

    @FXML
    private Label secondDetailLabel;

    @FXML
    private Label thirdDetailLabel;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button backButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button skipButton;

    @FXML
    private void initialize() {
        currentPageIndex = 0;
        renderCurrentPage();
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    @FXML
    private void showPreviousPage() {
        if (currentPageIndex == 0) {
            return;
        }

        currentPageIndex--;
        renderCurrentPage();
    }

    @FXML
    private void showNextPage() {
        if (currentPageIndex < PAGES.length - 1) {
            currentPageIndex++;
            renderCurrentPage();
            return;
        }

        finishOnboarding();
    }

    @FXML
    private void skipOnboarding() {
        finishOnboarding();
    }

    private void renderCurrentPage() {
        OnboardingPage page = PAGES[currentPageIndex];

        categoryLabel.setText(page.category());
        titleLabel.setText(page.title());
        descriptionLabel.setText(page.description());

        firstDetailLabel.setText(page.firstDetail());
        secondDetailLabel.setText(page.secondDetail());
        thirdDetailLabel.setText(page.thirdDetail());

        int currentStep = currentPageIndex + 1;

        stepLabel.setText(
                currentStep + " de " + PAGES.length
        );

        progressBar.setProgress(
                (double) currentStep / PAGES.length
        );

        backButton.setDisable(currentPageIndex == 0);

        boolean lastPage =
                currentPageIndex == PAGES.length - 1;

        nextButton.setText(
                lastPage
                        ? "Começar a usar"
                        : "Continuar"
        );

        skipButton.setVisible(!lastPage);
        skipButton.setManaged(!lastPage);
    }

    private void finishOnboarding() {
        if (onFinished != null) {
            onFinished.run();
        }
    }

    private record OnboardingPage(
            String category,
            String title,
            String description,
            String firstDetail,
            String secondDetail,
            String thirdDetail
    ) {
    }
}
package com.kayque.compensa.alerts.rule;

import com.kayque.compensa.alerts.model.SmartAlert;
import com.kayque.compensa.alerts.model.SmartAlertPriority;
import com.kayque.compensa.alerts.model.SmartAlertSnapshot;
import com.kayque.compensa.alerts.model.SmartAlertTopic;

import java.util.Objects;
import java.util.Optional;

public class WorkTimeAlertRule
        implements SmartAlertRule {

    private static final long ONE_WORK_DAY_MINUTES =
            8 * 60;

    private static final long ONE_WORK_WEEK_MINUTES =
            40 * 60;

    private static final long TWO_WORK_WEEKS_MINUTES =
            80 * 60;

    @Override
    public Optional<SmartAlert> evaluate(
            SmartAlertSnapshot snapshot
    ) {
        Objects.requireNonNull(
                snapshot,
                "O resumo financeiro é obrigatório."
        );

        long totalMinutes =
                snapshot.totalWorkMinutes();

        if (totalMinutes >= TWO_WORK_WEEKS_MINUTES) {
            return Optional.of(
                    createAlert(
                            "work-time.critical",
                            SmartAlertPriority.CRITICAL,
                            "Compras consumiram muito tempo de trabalho",
                            "As compras analisadas representam "
                                    + formatWorkTime(totalMinutes)
                                    + " de trabalho. Isso equivale a pelo menos "
                                    + "duas semanas completas da sua jornada."
                    )
            );
        }

        if (totalMinutes >= ONE_WORK_WEEK_MINUTES) {
            return Optional.of(
                    createAlert(
                            "work-time.attention",
                            SmartAlertPriority.ATTENTION,
                            "Atenção ao tempo convertido em compras",
                            "As compras analisadas representam "
                                    + formatWorkTime(totalMinutes)
                                    + " de trabalho, o equivalente a pelo menos "
                                    + "uma semana completa."
                    )
            );
        }

        if (totalMinutes >= ONE_WORK_DAY_MINUTES) {
            return Optional.of(
                    createAlert(
                            "work-time.informational",
                            SmartAlertPriority.INFORMATIONAL,
                            "Suas compras também custam tempo",
                            "As compras analisadas representam "
                                    + formatWorkTime(totalMinutes)
                                    + " do seu trabalho."
                    )
            );
        }

        return Optional.empty();
    }

    private SmartAlert createAlert(
            String code,
            SmartAlertPriority priority,
            String title,
            String message
    ) {
        return new SmartAlert(
                code,
                SmartAlertTopic.WORK_TIME,
                priority,
                title,
                message
        );
    }

    private String formatWorkTime(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (hours == 0) {
            return minutes + "min";
        }

        if (minutes == 0) {
            return hours + "h";
        }

        return hours + "h " + minutes + "min";
    }
}
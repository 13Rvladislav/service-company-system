package ru.servicecompany.requestservice.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.dto.response.RequestDetailsResponse;
import ru.servicecompany.requestservice.entity.WorkReport;
import ru.servicecompany.requestservice.enums.RequestStatus;
import ru.servicecompany.requestservice.enums.WorkReportStatus;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class WorkReportPdfService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public byte[] generate(
            RequestDetailsResponse request,
            WorkReport report
    ) {
        String html = buildHtml(request, report);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();

            builder.withHtmlContent(html, "");
            builder.useFont(
                    () -> {
                        InputStream font = getClass().getResourceAsStream(
                                "/fonts/DejaVuSans.ttf"
                        );

                        if (font == null) {
                            throw new IllegalStateException(
                                    "Не найден шрифт fonts/DejaVuSans.ttf"
                            );
                        }

                        return font;
                    },
                    "DejaVu Sans"
            );

            builder.useFastMode();
            builder.toStream(output);
            builder.run();

            return output.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Не удалось сформировать PDF-отчёт по заявке "
                            + request.id(),
                    exception
            );
        }
    }

    private String buildHtml(
            RequestDetailsResponse request,
            WorkReport report
    ) {
        String clientName = joinName(
                request.clientLastName(),
                request.clientFirstName(),
                request.clientMiddleName()
        );

        String masterName = request.master() == null
                ? "Не назначен"
                : joinName(
                request.master().lastName(),
                request.master().firstName(),
                request.master().middleName()
        );

        String masterPhone = request.master() == null
                ? "—"
                : value(request.master().phone());

        String requestStatus = requestStatusLabel(request.status());
        String reportStatus = reportStatusLabel(report.getStatus());

        return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        @page {
                            size: A4;
                            margin: 20mm 16mm 18mm 16mm;
                        }

                        body {
                            font-family: 'DejaVu Sans';
                            font-size: 10pt;
                            color: #243247;
                            line-height: 1.5;
                        }

                        .header {
                            background: #17365D;
                            color: white;
                            padding: 22px;
                            margin-bottom: 22px;
                        }

                        .company {
                            font-size: 9pt;
                            color: #D7E5F5;
                            letter-spacing: 1px;
                        }

                        h1 {
                            font-size: 20pt;
                            margin: 10px 0 4px 0;
                        }

                        .subtitle {
                            font-size: 10pt;
                            color: #D7E5F5;
                        }

                        h2 {
                            font-size: 12pt;
                            color: #17365D;
                            border-bottom: 1px solid #CBD5E1;
                            padding-bottom: 6px;
                            margin-top: 22px;
                        }

                        table {
                            width: 100%%;
                            border-collapse: collapse;
                            margin-top: 8px;
                        }

                        td {
                            padding: 8px;
                            border-bottom: 1px solid #E2E8F0;
                            vertical-align: top;
                        }

                        td.label {
                            width: 32%%;
                            color: #64748B;
                        }

                        .status {
                            background: #E8EFFA;
                            color: #17365D;
                            padding: 5px 9px;
                            font-weight: bold;
                        }

                        .content {
                            background: #F8FAFC;
                            padding: 12px;
                            border: 1px solid #E2E8F0;
                            white-space: pre-wrap;
                            overflow-wrap: break-word;
                        }

                        .footer {
                            margin-top: 28px;
                            padding-top: 10px;
                            border-top: 1px solid #CBD5E1;
                            color: #64748B;
                            font-size: 8pt;
                        }

                        .muted {
                            color: #64748B;
                        }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <div class="company">СЕРВИСНАЯ КОМПАНИЯ</div>
                        <h1>Отчёт о выполнении заявки</h1>
                        <div class="subtitle">
                            Номер заявки: %s
                        </div>
                    </div>

                    <h2>1. Информация о заявке</h2>
                    <table>
                        %s
                        %s
                        %s
                        %s
                        %s
                    </table>

                    <h2>2. Клиент и обслуживание</h2>
                    <table>
                        %s
                        %s
                        %s
                        %s
                    </table>

                    <h2>3. Результаты работ</h2>
                    %s

                    <h2>4. Диагностика и ремонт</h2>
                    %s

                    <h2>5. Результат выполнения</h2>
                    %s

                    <h2>6. Рекомендации</h2>
                    %s

                    <h2>7. Комментарий мастера</h2>
                    %s

                    <div class="footer">
                        Документ сформирован автоматически информационной
                        системой сервисной компании.<br/>
                        Дата формирования: %s
                    </div>
                </body>
                </html>
                """.formatted(
                escape(request.id().toString()),

                row("Статус заявки", requestStatus),
                row("Статус отчёта", reportStatus),
                row("Дата создания", format(request.createdAt())),
                row("Планируемый визит", format(request.preferredVisitAt())),
                row("Описание проблемы", value(request.problemDescription())),

                row("Клиент", clientName),
                row("Адрес обслуживания", value(report.getAddressSnapshot())),
                row("Оборудование", value(report.getEquipmentSnapshot())),
                row("Мастер", masterName + " · " + masterPhone),

                block(report.getProblemSnapshot()),

                block(
                        "Диагноз:\n" + value(report.getDiagnosis())
                                + "\n\nПричина неисправности:\n"
                                + value(report.getFailureCause())
                                + "\n\nВыполненные работы:\n"
                                + value(report.getPerformedWork())
                ),

                block(
                        value(report.getWorkResult())
                                + "\n\nДата завершения работ: "
                                + format(report.getCompletedAt())
                ),

                block(report.getRecommendations()),
                block(report.getMasterComment()),
                escape(LocalDateTime.now().format(DATE_FORMAT))
        );
    }

    private String row(String label, String content) {
        return """
                <tr>
                    <td class="label">%s</td>
                    <td>%s</td>
                </tr>
                """.formatted(escape(label), escape(content));
    }

    private String block(String content) {
        return "<div class=\"content\">" + escape(content) + "</div>";
    }

    private String joinName(String last, String first, String middle) {
        return (value(last) + " " + value(first) + " " + value(middle))
                .trim()
                .replaceAll("\\s+", " ");
    }

    private String value(String text) {
        return text == null || text.isBlank() ? "—" : text.trim();
    }

    private String format(LocalDateTime dateTime) {
        return dateTime == null
                ? "—"
                : dateTime.format(DATE_FORMAT);
    }

    private String escape(String text) {
        if (text == null) {
            return "—";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String requestStatusLabel(RequestStatus status) {
        if (status == null) {
            return "Не указан";
        }

        return switch (status) {
            case NEW -> "Новая";
            case WAITING_FOR_ASSIGNMENT -> "Ожидает назначения мастера";
            case ASSIGNED -> "Мастер назначен";
            case IN_PROGRESS -> "В работе";
            case REPORT_SUBMITTED -> "Отчёт отправлен";
            case REVISION_REQUIRED -> "Требуется доработка";
            case COMPLETED -> "Завершена";
            case CANCELLED -> "Отменена";
        };
    }

    private String reportStatusLabel(WorkReportStatus status) {
        if (status == null) {
            return "Не указан";
        }

        return switch (status) {
            case DRAFT -> "Черновик";
            case SUBMITTED -> "Отправлен клиенту";
            case ACCEPTED -> "Принят клиентом";
            case REWORK_REQUESTED -> "Возвращён на доработку";
        };
    }
}

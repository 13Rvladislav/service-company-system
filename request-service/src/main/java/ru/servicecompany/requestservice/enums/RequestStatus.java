
package ru.servicecompany.requestservice.enums;

/**
 * Статусы жизненного цикла заявки.
 */
public enum RequestStatus {

    // Заявка создана клиентом
    NEW,

    // Система ожидает назначения подходящего мастера
    WAITING_FOR_ASSIGNMENT,

    // Мастер назначен на заявку
    ASSIGNED,

    // Мастер принял заявку
    ACCEPTED,

    // Мастер выполняет работу
    IN_PROGRESS,

    // Мастер отправил отчёт о выполненной работе
    REPORT_SUBMITTED,

    // Клиент запросил доработку
    REVISION_REQUIRED,

    // Заявка успешно завершена
    COMPLETED,

    // Мастер отклонил назначение
    REJECTED,

    // Заявка отменена
    CANCELLED
}

const test = require("node:test");
const assert = require("node:assert/strict");
const {
  getAppointmentReminderDate,
} = require("../src/utils/appointmentReminder");

const now = new Date("2026-11-20T12:00:00-03:00");

test("schedules the reminder 24 hours before a future appointment", () => {
  const reminder = getAppointmentReminderDate(
    { data: "2026-11-22", horario: "15:30" },
    now,
  );

  assert.equal(reminder.toISOString(), "2026-11-21T18:30:00.000Z");
});

test("uses a one-hour reminder for appointments less than a day away", () => {
  const reminder = getAppointmentReminderDate(
    { data: "2026-11-20", horario: "18:30" },
    now,
  );

  assert.equal(reminder.toISOString(), "2026-11-20T20:30:00.000Z");
});

test("does not schedule reminders for past or imminent appointments", () => {
  assert.equal(
    getAppointmentReminderDate(
      { data: "2026-11-20", horario: "11:00" },
      now,
    ),
    null,
  );
  assert.equal(
    getAppointmentReminderDate(
      { data: "2026-11-20", horario: "12:30" },
      now,
    ),
    null,
  );
  assert.equal(
    getAppointmentReminderDate({ data: "not-a-date", horario: "10:00" }, now),
    null,
  );
});

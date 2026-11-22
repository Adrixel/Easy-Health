const DAY_IN_MS = 24 * 60 * 60 * 1000;
const HOUR_IN_MS = 60 * 60 * 1000;

function getAppointmentDate(booking) {
  if (!booking?.data || !booking?.horario) return null;

  const appointmentDate = new Date(
    `${booking.data}T${booking.horario}:00-03:00`,
  );
  return Number.isNaN(appointmentDate.getTime()) ? null : appointmentDate;
}

function getAppointmentReminderDate(booking, now = new Date()) {
  const appointmentDate = getAppointmentDate(booking);
  if (!appointmentDate) return null;

  const nowTime = now.getTime();
  const appointmentTime = appointmentDate.getTime();
  if (!Number.isFinite(nowTime) || appointmentTime <= nowTime) return null;

  const dayBefore = appointmentTime - DAY_IN_MS;
  const oneHourBefore = appointmentTime - HOUR_IN_MS;
  const reminderTime = dayBefore > nowTime ? dayBefore : oneHourBefore;

  return reminderTime > nowTime ? new Date(reminderTime) : null;
}

module.exports = { getAppointmentReminderDate };

import * as Notifications from "expo-notifications";
import { AndroidImportance, SchedulableTriggerInputTypes } from "expo-notifications";
import { Platform } from "react-native";
import { getAppointmentReminderDate } from "@/utils/appointmentReminder";
import { formatDateLong } from "@/utils/date";

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: false,
  }),
});

export const notificationService = {
  /** Cria o canal de notificação exigido pelo Android 8+ para notificações aparecerem. */
  async setupAndroidChannel() {
    if (Platform.OS === "android") {
      await Notifications.setNotificationChannelAsync("default", {
        name: "Easy Health",
        importance: AndroidImportance.HIGH,
        vibrationPattern: [0, 250, 250, 250],
        lightColor: "#047857",
        sound: "default",
      });
    }
  },

  async requestPermission() {
    if (Platform.OS === "web") return false;

    await this.setupAndroidChannel();
    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    const { status } =
      existingStatus === "granted"
        ? { status: existingStatus }
        : await Notifications.requestPermissionsAsync();

    return status === "granted";
  },

  async hasAppointmentReminder(bookingId) {
    if (Platform.OS === "web") return false;

    const scheduled = await Notifications.getAllScheduledNotificationsAsync();
    return scheduled.some(
      (notification) =>
        notification.content.data?.type === "appointment-reminder" &&
        notification.content.data?.bookingId === bookingId,
    );
  },

  async scheduleAppointmentReminder(booking) {
    if (Platform.OS === "web") return { status: "unavailable" };

    const reminderDate = getAppointmentReminderDate(booking);
    if (!reminderDate) return { status: "too-soon" };

    if (!(await this.requestPermission())) {
      return { status: "permission-denied" };
    }

    const scheduled = await Notifications.getAllScheduledNotificationsAsync();
    const existingReminders = scheduled.filter(
      (notification) =>
        notification.content.data?.type === "appointment-reminder" &&
        notification.content.data?.bookingId === booking.id,
    );
    await Promise.all(
      existingReminders.map((notification) =>
        Notifications.cancelScheduledNotificationAsync(notification.identifier),
      ),
    );

    const identifier = await Notifications.scheduleNotificationAsync({
      content: {
        title: "Lembrete de consulta",
        body: `${booking.especialidade || "Consulta"} em ${formatDateLong(booking.data)} às ${booking.horario}.`,
        data: {
          type: "appointment-reminder",
          bookingId: booking.id,
        },
        sound: "default",
      },
      trigger: {
        type: SchedulableTriggerInputTypes.DATE,
        date: reminderDate,
        channelId: "default",
      },
    });

    return { status: "scheduled", identifier, reminderDate };
  },

};

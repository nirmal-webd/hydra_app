# 💧 Hydra: Context-Aware Hydration

**A Product Vision & Use Case Document**

## 🚨 The Problem: Why Another Water Tracker?

We all know we need to drink more water, yet almost everyone fails to reach their daily goals. The market is flooded with hydration apps, but they all suffer from the same fatal product flaw: **They are dumb alarms.**

Standard apps buzz your phone at rigid intervals (e.g., exactly every 2 hours). If you are driving, in a deep state of work, or presenting in a meeting, you will simply swipe the notification away and forget about it. Once swiped, the app leaves you alone until the next arbitrary interval. 

They fail because they interrupt you when you are busy, and give up too easily when you ignore them.

## 🎯 The Solution: Hydra

Hydra is designed to behave less like a clock, and more like an aggressively polite personal assistant. It uses **context-aware triggers** to wait for the exact moment you have free time, and it refuses to be ignored.

### Core Philosophy
**Don't interrupt deep work; ambush the transitional moments.**

Instead of firing on a rigid timer, Hydra waits for the user to transition between tasks. The strongest signal for a "transitional moment" in modern life is **unlocking your phone**. 

## ✨ Key Product Features & Use Cases

### 1. The "Ambush on Unlock" (Context-Aware Delivery)
**Use Case:** You've been working on your laptop for 2 hours. Your hydration cooldown expired an hour ago, but Hydra didn't buzz and distract your workflow. The moment you close your laptop, pick up your phone, and unlock it to check Instagram, Hydra instantly pops down from the top of the screen: *"Drink Water."*
* **PM Value:** Drastically increases conversion (logging water) because the prompt is delivered at the exact second the user demonstrates they have device-attention to spare.

### 2. Relentless Auto-Snooze
**Use Case:** You are watching a YouTube video when a Hydra reminder pops up. You ignore it because the video is getting good. Instead of going away forever, Hydra silently waits 10 minutes and pops up *again*. It will do this up to 3 times. If you completely ignore it, it eventually packs up and hides, waiting to ambush you the *next* time you unlock your phone.
* **PM Value:** Prevents notification blindness. If the user is passive, the app gently escalates urgency, ensuring the reminder isn't lost in a sea of other notifications.

### 3. Smart "Screen-Off" Pausing
**Use Case:** A reminder pops up while your phone is on your desk. You walk away to grab a coffee. Your phone screen times out and turns off. Instead of buzzing an empty room 3 more times, Hydra detects that the screen turned off, instantly cancels the notification, and goes into stealth mode. When you return to your desk and unlock your phone, it immediately pops back up.
* **PM Value:** Saves battery, prevents annoying coworkers, and guarantees the notification is only shown to eyeballs, not empty chairs.

### 4. Sticky Notifications (Anti-Dismissal)
**Use Case:** You get a reminder but you feel lazy, so you try to swipe it away to clear your notification tray. The notification refuses to leave. You *must* interact with it—either by logging water, or explicitly pressing the "Snooze" or "Not Now" buttons.
* **PM Value:** Forces a micro-decision. By removing the easiest exit route (the swipe), the user is forced to consciously acknowledge their hydration habits.

### 5. Quiet Hours
**Use Case:** It's 10:00 PM and you are winding down. You unlock your phone in bed. Hydra realizes it's your configured Quiet Hours and suppresses all reminders so you don't have to get out of bed to drink water.
* **PM Value:** Builds user trust. An aggressive app must be incredibly respectful of boundaries, or it will be uninstalled.

## 📈 Success Metrics (KPIs)
To measure if Hydra is successfully building habits, we track:
1. **Log Conversion Rate:** What percentage of `SHOWING` notifications result in a `WaterLogged` event vs a `USER_DISMISSED` (Not Now) event?
2. **Goal Completion Rate:** The percentage of days a user hits their target volume.
3. **Snooze Exhaustion Rate:** How often does the app have to buzz 3 times before giving up? (Lower is better).

---

### Summary
Hydra isn't a timer; it's a state machine tied to human behavior. It respects your focus, protects your sleep, and relentlessly exploits your idle screen time to build a healthier habit.

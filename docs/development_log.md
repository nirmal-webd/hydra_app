# The Story of Hydra: How We Built It

This document is the story of how we built the Hydra app. Whenever we make a big change, try a new idea, or throw away an old one, we will write it down here. We use plain, simple language so anyone can understand why we made the choices we did.

---

## 1. How We Started
We set out to build a smart water-drinking assistant. Most water apps buzz your phone while it's in your pocket, which is annoying. 

Our core idea was simple: **Hydra should only remind you to drink water at the exact moment you unlock your phone.**

## 2. The "Too Many Sticky Notes" Problem
At first, we tried to build the brain of the app by leaving little "sticky notes" for it to remember things. One note said "is the screen locked?", another said "has the timer finished?", and another said "did they snooze?". 

Very quickly, the app's brain became covered in too many sticky notes. It started getting confused, reading the wrong notes at the wrong time, and behaving unpredictably.

## 3. The "Traffic Light" Solution (Finite State Machine)
**Why we changed things:** To fix the confusion, we threw away the sticky notes and replaced the app's brain with a simple **Traffic Light System** (in programming, this is called a Finite State Machine).

Just like a real traffic light can only ever be Green, Yellow, or Red, we made a strict rule: the app can only ever be in one single "State" at a time. For example:
- **Cooldown (Red Light):** The app is silently waiting. Do nothing.
- **Ready (Yellow Light):** The timer is done. Wait for the user to unlock their phone.
- **Snoozing (Flashing Light):** The user asked for 10 more minutes of peace.

Because the app can only be in one state at a time, it is impossible for it to get confused. It always knows exactly what it is supposed to be doing.

## 4. Learning from Mistakes (Our "Undos")

Sometimes we built a feature, realized it was a bad idea, and deleted it. Here is why we undid certain things:

### 4.1. Undoing the "Goal Reached" Traffic Light
- **What we tried:** We added a special traffic light just for when you reach your daily water goal (e.g., 2 liters). When this light turned on, the app stopped doing anything.
- **Why we deleted it:** We realized that reaching your goal shouldn't break the traffic light system! What if you wanted to drink more water than your goal? By making it a rigid traffic light, we broke the app. We deleted that light. Now, the system keeps running normally, but if you've hit your goal, it just chooses to stay quiet instead of buzzing you.

### 4.2. Undoing the "Instant Nag" 
- **What we tried:** When you first opened the app or restarted your phone, we forced the app to instantly jump to the "Ready" state. This meant the very next time you unlocked your phone, it would nag you to drink water.
- **Why we deleted it:** If a user specifically told the settings, "Only bother me every 2 hours," they would get very annoyed if the app nagged them instantly after turning their phone on. We deleted the instant nag. Now, when the app starts up, it respects the user and politely waits for the 2-hour timer to finish first.

### 4.3. The Billboard vs. The Sticky Note
- **What we tried:** We built a massive, beautiful "Billboard" (a full-screen pop-up) that would take over your entire screen to remind you to drink water.
- **Why we deleted it:** To show a billboard over other apps, Android forces us to ask the user for a very scary-sounding security permission ("Display over other apps"). When users see this, they get worried we are spying on them, and they delete the app. 
- **The Solution:** We tore down the billboard. Instead, we created a **Sticky Notification**. It sits in your notification drawer and physically cannot be swiped away. You *must* click a button on it (like "Drank" or "Skip") to get rid of it. It accomplishes the exact same goal—making sure you don't ignore it—but without asking for scary permissions!

### 4.4. Making the Buttons Fit
- **What we tried:** The buttons on our Sticky Notification used to say `"💧 Drank!"`, `"⏰ 10 min"`, and `"🚫 Not Now"`.
- **Why we changed it:** On smaller phones, those words were too long and got chopped off in the middle. Since Android doesn't let us shrink the text size, we had to shrink the words themselves. We changed them to `"💧 250ml"`, `"⏰ 10 min"`, and `"🚫 Skip"` so they look perfect on every phone.

### 4.5. The Dashboard Redesign (Removing the Clutter)
- **What we tried:** At first, the main screen had a "Stats Card" that showed your current streak, milliliters remaining, and how many times the app reminded you today.
- **Why we changed it:** The card was confusing. The "remaining ml" was already shown in the big circular progress ring, and users didn't care about the "reminders shown" number.
- **The Solution:** We deleted the stats card. Instead, we added a clear **Weekly Tracker**. It shows a row of 7 days with simple icons: a green check (goal met), a red X (goal missed), or a grey dash (no data). Below that, we show a large "Fire" 🔥 streak number to motivate you. This makes it instantly obvious how well you are doing this week without needing to read numbers.
  - *Refinement:* We later highlighted the *current* day with a bold text color and a circular border so it stands out. We also changed the logic so the current day doesn't show a discouraging red 'X' in the morning—it shows a forgiving orange 'in progress' dash until the day is actually over!

### 4.6. The Midnight Rollover Bug
- **What happened:** A user noticed that if the app stayed open in memory past midnight, the dashboard and notification would still show yesterday's water total instead of resetting to 0. 
- **Why it happened:** The database query was using static "start of day" and "end of day" timestamps calculated *exactly once* when the app launched. Because time passed but the app never completely restarted, the query stayed locked in the past.
- **The Solution:** We updated the `WaterRepository` to use a "ticking" time-stream that recalculates what "today" means every 60 seconds. When midnight strikes, the time-stream rolls over, automatically instructing the database to switch its query to the new day, instantly resetting the UI across the entire app.

### 4.7. Refining Notification Actions
- **What happened:** We realized that having a "Skip" button on the sticky notification created a weird state where the reminder just waited in the background until the very next time the user unlocked their phone.
- **The Solution:** We entirely removed the "Skip" button! Now, the notification strictly offers three paths: "💧 250ml", "✏️ Custom" (which opens the app to log a specific amount), and "⏰ 10 min" (Snooze). This reinforces the core loop: you must either hydrate now, tell us exactly how much you hydrated, or explicitly ask for more time.

---

## 5. Going Forward
Whenever we add a big new feature, change our minds, or delete something important, we will come back to this document and explain the story of *why* we did it in plain, simple English.

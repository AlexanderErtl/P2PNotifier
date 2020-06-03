from pynotifier import Notification

def notify(title : str, msg : str) -> None:
    print(f"Sending notification: Title: '{title}' Message: '{msg}'")
    Notification(
	title=title,
	description=msg,
        # Duration in seconds
	duration=5,
	urgency=Notification.URGENCY_NORMAL
    ).send()

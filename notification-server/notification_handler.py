from pynotifier import Notification

def notify(title : str, msg : str) -> None:
    Notification(
	title=title,
	description=msg,
        # Duration in seconds
	duration=5,
	urgency=Notification.URGENCY_NORMAL
    ).send()

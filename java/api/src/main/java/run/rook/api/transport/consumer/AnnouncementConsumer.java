package run.rook.api.transport.consumer;

import run.rook.api.RID;

public interface AnnouncementConsumer {
	void onAnnouncement(RID serviceId);
}

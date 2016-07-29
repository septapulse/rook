package rook.api.transport.consumer;

import rook.api.RID;

public interface AnnouncementConsumer {
	void onAnnouncement(RID serviceId);
}

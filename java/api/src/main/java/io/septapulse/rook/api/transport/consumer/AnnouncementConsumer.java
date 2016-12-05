package io.septapulse.rook.api.transport.consumer;

import io.septapulse.rook.api.RID;

public interface AnnouncementConsumer {
	void onAnnouncement(RID serviceId);
}

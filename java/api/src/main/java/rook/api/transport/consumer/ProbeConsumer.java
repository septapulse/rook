package rook.api.transport.consumer;

import rook.api.RID;

public interface ProbeConsumer {
	void onProbe(RID from);
}

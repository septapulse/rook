package run.rook.api.transport.consumer;

import run.rook.api.RID;

public interface ProbeConsumer {
	void onProbe(RID from);
}

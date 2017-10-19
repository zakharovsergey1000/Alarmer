package biz.advancedcalendar;

import android.content.Context;

import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

public class ReportSenderImpl implements ReportSender {
	public ReportSenderImpl() {
		// initialize your sender with needed parameters
	}

	@Override
	public void send(Context context, CrashReportData report) throws ReportSenderException {
		// Iterate over the CrashReportData instance and do whatever
		// you need with each pair of ReportField key / String value
	}
}
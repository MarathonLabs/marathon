package com.malinskiy.marathon.vendor.junit4.runner;

import com.malinskiy.marathon.vendor.junit4.runner.contract.Frame;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ListenerAdapter extends RunListener {
    final private Socket socket;

    public ListenerAdapter(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        super.testRunStarted(description);
        send(socket, Frame.Message.newBuilder()
            .setType(Frame.Message.Type.RUN_STARTED)
            .build()
        );
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        super.testRunFinished(result);
        send(socket, Frame.Message.newBuilder()
            .setType(Frame.Message.Type.RUN_FINISHED)
            .setTotalDurationMillis(result.getRunTime())
            .build()
        );
    }

    @Override
    public void testSuiteStarted(Description description) throws Exception {
        super.testSuiteStarted(description);
    }

    @Override
    public void testSuiteFinished(Description description) throws Exception {
        super.testSuiteFinished(description);
    }

    @Override
    public void testStarted(Description description) throws Exception {
        super.testStarted(description);
        send(socket, Frame.Message.newBuilder()
            .setType(Frame.Message.Type.TEST_STARTED)
            .setClassname(description.getClassName())
            .setMethod(description.getMethodName())
            .build()
        );
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testFinished(description);
        send(socket, Frame.Message.newBuilder()
            .setType(Frame.Message.Type.TEST_FINISHED)
            .setClassname(description.getClassName())
            .setMethod(description.getMethodName())
            .build()
        );
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
        send(socket, Frame.Message.newBuilder()
            .setType(Frame.Message.Type.TEST_FAILURE)
            .setClassname(failure.getDescription().getClassName())
            .setMethod(failure.getDescription().getMethodName())
            .setMessage(failure.getMessage())
            .setStacktrace(failure.getTrace())
            .build()
        );
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        super.testAssumptionFailure(failure);
        send(socket, Frame.Message.newBuilder()
            .setType(Frame.Message.Type.TEST_ASSUMPTION_FAILURE)
            .setClassname(failure.getDescription().getClassName())
            .setMethod(failure.getDescription().getMethodName())
            .setMessage(failure.getMessage())
            .setStacktrace(failure.getTrace())
            .build()
        );
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        super.testIgnored(description);
        send(socket, Frame.Message.newBuilder()
            .setType(Frame.Message.Type.TEST_IGNORED)
            .setClassname(description.getClassName())
            .setMethod(description.getMethodName())
            .build()
        );
    }

    private void send(Socket socket, Frame.Message message) {
        try {
            OutputStream stream = socket.getOutputStream();
            message.writeDelimitedTo(stream);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

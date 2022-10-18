package com.bupt.deck;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.work.ListenableWorker.Result;
import androidx.work.testing.TestWorkerBuilder;

import com.bupt.deck.checkers.NetworkChecker;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NetworkCheckerTest {

    private Context context;
    private Executor executor;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        executor = Executors.newSingleThreadExecutor();
    }

    @Test
    public void testNetworkCheckWorker() {
        NetworkChecker worker = TestWorkerBuilder.from(
                context,
                NetworkChecker.class,
                executor).build();
        Result ret = worker.doWork();
        assertThat(ret, is(Result.success()));
    }
}
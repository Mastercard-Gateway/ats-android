package com.mastercard.gateway.sample;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mastercard.gateway.ats.ATSClient;
import com.mastercard.gateway.ats.domain.ATSMessage;
import com.mastercard.gateway.ats.domain.CardRequestType;
import com.mastercard.gateway.ats.domain.CardServiceRequest;
import com.mastercard.gateway.ats.domain.CardServiceResponse;
import com.mastercard.gateway.ats.domain.DeviceRequest;
import com.mastercard.gateway.ats.domain.DeviceResponse;
import com.mastercard.gateway.ats.domain.RequestResultType;
import com.mastercard.gateway.ats.domain.TotalAmountType;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AmountActivity extends Activity implements ATSClient.Callback {

    EditText amountEditText;

    Action action;

    ATSClient atsClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount);

        ((SampleApplication) getApplication()).getAtsClient().addCallback(this);

        atsClient = ((SampleApplication) getApplication()).getAtsClient();
        amountEditText = findViewById(R.id.amountEditText);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmountActivity.this.finish();
            }
        });
        Button actionButton = findViewById(R.id.button_action);

        if (getIntent().getStringExtra("Action").equals("Payment")) {
            action = Action.Payment;
            toolbar.setTitle("Create a payment");
            actionButton.setText("Pay");
        } else {
            action = Action.Authorization;
            toolbar.setTitle("Create an authorization");
            actionButton.setText("Auth");
        }

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = amountEditText.getText().toString();

                createATSMessage(amount);
            }
        });
    }


    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onMessageReceived(@Nullable ATSMessage message) {
        if (message instanceof CardServiceResponse) {
            CardServiceResponse response = (CardServiceResponse) message;


            Intent intent = new Intent(AmountActivity.this, ResultActivity.class);
            intent.putExtra("Action", action.equals(Action.Payment) ? "Payment" : "Authorization");

            if (response.overallResult.equals(RequestResultType.Success)) {
                intent.putExtra("Result", "Success");
            } else {
                intent.putExtra("Result", "Error");
            }

            atsClient.close();

            startActivity(intent);

            finish();
        } else if (message instanceof DeviceRequest) {
            DeviceRequest request = (DeviceRequest) message;

            DeviceResponse response = new DeviceResponse();
            response.setRequestID(request.getRequestID());
            response.setRequestType(request.getRequestType());
            response.setWorkstationID(request.getWorkstationID());
            response.setPopid(request.getPopid());
            response.setApplicationSender(request.getApplicationSender());
            response.setOverallResult(RequestResultType.Success);

            List<DeviceResponse.Output> responseOuputs = new ArrayList<>();
            for (DeviceRequest.Output output : request.getOutput()) {
                DeviceResponse.Output responseOutput = new DeviceResponse.Output();
                responseOutput.setOutDeviceTarget(output.getOutDeviceTarget());
                responseOutput.setOutResult(RequestResultType.Success);
                responseOuputs.add(responseOutput);
            }

            response.setOutput(responseOuputs);


            atsClient.sendMessage(response);
        }
    }

    @Override
    public void onError(@NotNull Throwable throwable) {
        Intent intent = new Intent(AmountActivity.this, ResultActivity.class);
        intent.putExtra("Action", action.equals(Action.Payment) ? "Payment" : "Authorization");
        intent.putExtra("Result", "Error");
        startActivity(intent);
    }

    private void createATSMessage(String amount) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String workstationID = preferences.getString("ATS_WORKSTATION_ID", "");
        String popID = preferences.getString("ATS_POP_ID", "");

        CardServiceRequest request = new CardServiceRequest();
        request.setRequestType(action == Action.Payment ? CardRequestType.CardPayment : CardRequestType.CardPreAuthorisation);
        request.setWorkstationID(workstationID);
        request.setRequestID("179");
        request.setPopid(popID);

        CardServiceRequest.POSdata posData = new CardServiceRequest.POSdata();
        posData.setPosTimeStamp(new Date());
        posData.setTransactionNumber(150);
        request.setPoSdata(posData);

        TotalAmountType totalAmountType = new TotalAmountType();
        totalAmountType.value = new BigDecimal(amount);
        totalAmountType.setPaymentAmount(new BigDecimal(amount));
        request.setTotalAmount(totalAmountType);

        atsClient.sendMessage(request);

        findViewById(R.id.transaction_in_progress).setVisibility(View.VISIBLE);
        findViewById(R.id.collect_amount).setVisibility(View.GONE);
    }


    enum Action {
        Authorization,
        Payment
    }
}

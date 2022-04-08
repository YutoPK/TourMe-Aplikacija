package com.example.tourme.Notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                "Content-Type:application/json",
                "Authorization:key=AAAAIRphqDI:APA91bEyLMX7iN1e1cPhCaf5KJfVbpSwk2S-MCb3CCSWcrhg3iIg9WfxLDKLFaexWBEB9ayr61JVrprCLUqeskU1eT_LfMr4jWxHURlXNmMz_pYC5JVP_yIC49QILsFsVb_8_I4gJqCa"
        }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}

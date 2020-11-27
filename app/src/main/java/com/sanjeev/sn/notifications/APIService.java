package com.sanjeev.sn.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAS1mumLk:APA91bGPib3Y4cnCEFzGp1Z7aZJcJK04zcIbrBqNo4hnbPhxlP6GX3YOrFQVD-KViHAveM8s193PfUbE5pxOYvHJf0jSrSu8FL9TTfZ1SEcYqe3Ou156rGLofIldsrVLTM_yZqPOe2b_"


    })

    @POST("fcm/send")
    Call<Response> sendNotifications(@Body Sender body);
}

package com.javatechie.service;

import com.javatechie.grpc.StockRequest;
import com.javatechie.grpc.StockResponse;
import com.javatechie.grpc.StockTradingServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class StockClientService {

//   @GrpcClient("stockService") // This should match the client name in application.yml
//    private StockTradingServiceGrpc.StockTradingServiceBlockingStub stockServiceStub;

    @GrpcClient("stockService") // This should match the client name in application.yml
    private StockTradingServiceGrpc.StockTradingServiceStub stockServiceStub;



    public void subscribeStockPrice(String stockSymbol) {
        StockRequest request = StockRequest.newBuilder()
                .setStockSymbol(stockSymbol)
                .build();
        stockServiceStub.subscribeStockPrice(request, new StreamObserver<StockResponse>() {
            @Override
            public void onNext(StockResponse response) {
                System.out.println("Stock Price Update: " + response.getStockSymbol() +
                        " Price: " + response.getPrice() + " "+
                        " Time: " + response.getTimestamp());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Stock price streaming completed.");
            }
        });
    }


}

package com.javatechie.service;

import com.javatechie.entity.Stock;
import com.javatechie.grpc.StockRequest;
import com.javatechie.grpc.StockResponse;
import com.javatechie.grpc.StockTradingServiceGrpc;
import com.javatechie.repository.StockRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@GrpcService
public class StockTradingServiceImpl extends StockTradingServiceGrpc.StockTradingServiceImplBase {

    private final StockRepository stockRepository;

    public StockTradingServiceImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public void getStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        String stockSymbol = request.getStockSymbol();
        Optional<Stock> stock = stockRepository.findByStockSymbol(stockSymbol);

        if (stock.isPresent()) {
            Stock stockEntity = stock.get();
            StockResponse response = StockResponse.newBuilder()
                    .setStockSymbol(stockEntity.getStockSymbol())
                    .setPrice(stockEntity.getPrice())
                    .setTimestamp(stockEntity.getLastUpdated() != null ? stockEntity.getLastUpdated().toString() : "N/A")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Stock " + stockSymbol + " is not available in system")
                            .asRuntimeException()
            );
        }
    }


    @Override
    public void subscribeStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        String stockSymbol = request.getStockSymbol();
        try {
            // Send price updates for 10 seconds
            for (int i = 0; i < 10; i++) {
                StockResponse response = StockResponse.newBuilder()
                        .setStockSymbol(stockSymbol)
                        .setPrice(new Random().nextDouble(200))
                        .setTimestamp(Instant.now().toString())
                        .build();

                responseObserver.onNext(response); // Send response to client
                TimeUnit.SECONDS.sleep(1); // Simulate real-time update every second
            }
            responseObserver.onCompleted(); // End the stream
        } catch (InterruptedException e) {
            responseObserver.onError(e);
        }
    }
}

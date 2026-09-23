package com.levy.crypto.service.market;
import com.levy.crypto.model.MarketTicker;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LatestMarketState {

    private final Map<String, MarketTicker> state = new HashMap<>();

    public void addTicker(MarketTicker ticker) {
        state.put(ticker.getSymbol(), ticker);
    }

    public Map<String, MarketTicker> getAll() {
        return state;
    }
}

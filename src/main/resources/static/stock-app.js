import {clearChildren, createLogger, createSlider, createWSConnection, removeChildren} from "./showcase-utils.js";
import {newChart} from "./stock-app-chart.js";

export function createApp(wsAddress) {
    const PRICES = document.getElementById("prices");
    const LOGGER = createLogger('logs');
    const MONITOR = document.getElementById('rule-monitor');
    const BUTTON_STOP = document.getElementById('stop-button');
    const BUTTON_START = document.getElementById('run-button');
    const DELAY_SLIDER = createSlider('delay', {
        min: 0,
        max: 1000,
        value: 200
    });
    const CHART = newChart();

    const SOCKET = createWSConnection(wsAddress, {
        'onMessage': (m) => onMessage(m),
        'onError': (e) => onError(e),
        'onClose': (e) => onClose(e),
        'onOpen': function () {
            LOGGER.log("Connection established");
        }
    });

    let ruleHistory = [];
    let ruleIds = [];
    // This will hold the price feed state
    let DATA = {
        currentIndex: 0,
        prices: []
    }

    function onClose(evt) {
        LOGGER.log('Session closed, reason: ' + evt.reason);
    }

    function onError(err) {
        LOGGER.error(err);
    }

    function onMessage(msg) {
        switch (msg.type) {
            case 'CONFIG': {
                // Initial greeting with default rule and price history
                const payload = JSON.parse(msg.payload);
                // Init stocks editor
                PRICES.textContent = payload['prices'];
                validateStockData();
                // Init rules
                initRules(payload['rules']);
                // Set run/stop controls
                setControls([true, false]);
                LOGGER.log('Default rules and data received from the server')
                break;
            }
            case 'LOG': {
                LOGGER.log(msg.payload);
                break
            }
            case 'RULE_ACTIVATION': {
                onRuleActivation(msg.payload);
                break
            }
            case 'PRICE_INDICATOR': {
                const ind = JSON.parse(msg.payload);
                CHART.updateLine(ind.name, ind.id, ind.value);
                break
            }
            case 'TREND_INFO': {
                const t = JSON.parse(msg.payload);
                CHART.drawMessage(t.id, t.message);
                LOGGER.log("Trade event '" + t.message + "' at position: " + t.id);
                break
            }
            case 'ERROR': {
                LOGGER.error(msg.payload);
                setControls([true, false]);
                break;
            }
            case 'READY': {
                if (DATA.currentIndex === DATA.prices.length) {
                    // No more data
                    SOCKET.write('STOP');
                } else {
                    playRuleHistory(() => {
                        SOCKET.write('OHLC', DATA.prices[DATA.currentIndex]);
                        CHART.setCurrent(DATA.currentIndex);
                        DATA.currentIndex++;
                    });
                }
                break;
            }
            case 'STOPPED': {
                onSessionEnd();
                break;
            }
        }
    }

    function playRuleHistory(afterFunction) {
        const delay = DELAY_SLIDER.value() / ruleHistory.length;
        playRule(0, delay, afterFunction)
    }

    function playRule(ruleIndex, delay, afterFunction) {
        if (ruleIndex < ruleHistory.length) {
            const ruleId = ruleHistory[ruleIndex];
            highlightRule(ruleId);
            setTimeout(function () {
                playRule(ruleIndex + 1, delay, afterFunction)
            }, delay)
        } else {
            ruleHistory = [];
            afterFunction();
        }
    }

    function initRules(rules) {
        removeChildren(MONITOR);
        rules.forEach(r => {
            const item = document.createElement('li');
            item.classList.add("list-group-item", "d-flex", "justify-content-between", "align-items-start")
            item.id = r.id;
            const d = document.createElement('div');
            d.classList.add("me-auto")
            d.innerText = r.name;

            const s = document.createElement('span');
            s.classList.add("badge", "bg-primary", "rounded-pill", "visually-hidden", "font-monospace", "small");
            s.innerText = '0';
            s.id = 'count-' + r.id;
            item.append(d, s);
            MONITOR.append(item);
            ruleIds.push(r.id);
        })
    }

    function onSessionEnd() {
        setControls([true, false]);
        LOGGER.log('Session ended');
        const idx = DATA.currentIndex;
        if (idx === DATA.prices.length) {
            // The last index, meaning session hasn't been interrupted
            CHART.hideShades();
        }
    }

    function highlightRule(ruleId) {
        // Find and update specific counter
        const counter = document.getElementById('count-' + ruleId)
        if (counter) {
            const curVal = Number.parseInt(counter.innerText);
            counter.innerText = (curVal + 1).toString();
            counter.classList.remove("visually-hidden");
        }
        // Updating the whole list of rules
        const nodes = MONITOR.children;
        for (let i = 0; i < nodes.length; i++) {
            const node = nodes.item(i);
            if (node.id === ruleId) {
                node.classList.add('list-group-item-secondary');
            } else {
                node.classList.remove('list-group-item-secondary');
            }
        }
    }

    function onRuleActivation(ruleId) {
        ruleHistory.push(ruleId);
    }

    function setControls(arr) {
        BUTTON_START.disabled = !arr[0];
        BUTTON_STOP.disabled = !arr[1];
    }


    function validateStockData() {
        try {
            let prices = JSON.parse(PRICES.value);
            if (Array.isArray(prices)) {
                for (let i = 0; i < prices.length; i++) {
                    const ohlc = prices[i];
                    const o = ohlc['open'];
                    const h = ohlc['high'];
                    const l = ohlc['low'];
                    const c = ohlc['close'];
                    if (isNaN(o) || isNaN(h) || isNaN(l) || isNaN(c)) {
                        LOGGER.error("Not a number at " + i);
                        return false;
                    }

                    if (Math.max(o, l, c) > h) {
                        LOGGER.error("'" + h + "' is not the highest value at data index: " + i);
                        return false;
                    }
                    if (Math.min(o, h, c) < l) {
                        LOGGER.error("'" + h + "' is not the lowest value at data index: " + i);
                        return false;
                    }
                }

                // Saving prices locally, that's where we will stream them from
                DATA.prices = prices;
                // Init chart
                CHART.initChart(prices);

                return true;
            } else {
                LOGGER.error("Invalid JSON format (not an Array)");
            }

        } catch (e) {
            console.error(e);
            LOGGER.error("Invalid format")
        }
        return false;
    }


    return {
        sessionStart: function () {
            // Clearing the logs
            clearChildren('clearable');

            // Clearing counters
            ruleIds.forEach(id => {
                const counter = document.getElementById('count-' + id);
                counter.innerText = '0';
                counter.classList.add("visually-hidden");
            })

            // Clearing rule activation history
            ruleHistory = [];

            LOGGER.log("Validating historical prices...");
            if (validateStockData()) {
                setControls([false, true]);
                DATA.currentIndex = 0;
                CHART.clearPathsGroup();
                SOCKET.write('START');
            } else {
                LOGGER.error('Invalid price data, please correct.')
            }
        },

        sessionStop: function () {
            SOCKET.write('STOP');
            LOGGER.log('STOP signal sent');
        },

        closeConnection: function () {
            SOCKET.close();
        }
    }
}



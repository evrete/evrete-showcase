import {clearChildren, createLogger, createSlider, createWSConnection, removeChildren} from "./showcase-utils.js";


const SVG_NS = "http://www.w3.org/2000/svg";
const IMG_SIZE = 2048;
const MAX_ZOOM = 3;


export function town(socketAddress) {
    const SVG = document.getElementById('map-svg');
    const LOGGER = createLogger('logs');
    const BUTTON_STOP = document.getElementById('stop-button');
    const BUTTON_START = document.getElementById('start-button');
    const INTERVAL = document.getElementById('interval');
    const DELAY = document.getElementById('delay');
    const RULES = document.getElementById('rules');
    const ZOOM_IN = document.getElementById('zoom-in-button');
    const ZOOM_OUT = document.getElementById('zoom-out-button');
    const RESOLUTION = document.getElementById('resolution');

    ZOOM_IN.onclick = () => _zoom(1);
    ZOOM_OUT.onclick = () => _zoom(-1);


    const SOCKET = createWSConnection(socketAddress,
        {
            'onMessage': (m) => onMessage(m),
            'onError': (e) => onError(e),
            'onClose': (e) => onClose(e),
            'onOpen': function () {
                LOGGER.log("Connection established")
            }
        }
    );

    const SVG_DUMMY_POINT = SVG.createSVGPoint();

    const CONFIG = {
        viewport: {
            resolution: undefined,
            zoom: 0,
            x: 0,
            y: 0
        },
        workingPercent: undefined,
        maxPopulation: undefined,
        commuteSpeed: undefined,
        leisureAtHomePercent: undefined
    };

    SVG.onclick = function (ev) {
        SVG_DUMMY_POINT.x = ev.clientX;
        SVG_DUMMY_POINT.y = ev.clientY;
        let converted = SVG_DUMMY_POINT.matrixTransform(SVG.getScreenCTM().inverse());

        const clickPosition = {
            x: Math.round(converted.x),
            y: Math.round(converted.y)
        }

        centerView(clickPosition)
    }

    function onClose(evt) {
        LOGGER.log('Session closed, reason: ' + evt.reason);
    }

    function onError(err) {
        console.error("Error", err);
        LOGGER.error(err);
    }

    function _zoom(delta) {
        let currentZoom = CONFIG.viewport.zoom;
        if (delta < 0 && currentZoom === 0) {
            return;
        }
        if (delta > 0 && currentZoom === MAX_ZOOM) {
            return;
        }
        CONFIG.viewport.zoom = currentZoom + delta;
        centerView();
    }

    /**
     * Centers the map around desired center
     *
     * @param desiredCenter desired center
     */
    function centerView(desiredCenter) {
        const center = desiredCenter || svgCenter();
        let factor = 1 << CONFIG.viewport.zoom;

        let halfSize = Math.round(0.5 * IMG_SIZE / factor);
        let size = halfSize * 2;
        let imgX = Math.round(center.x - halfSize);
        let imgY = Math.round(center.y - halfSize);

        if (imgX < 0) {
            imgX = 0;
        }

        if (imgX + size > IMG_SIZE) {
            imgX = IMG_SIZE - size;
        }

        if (imgY < 0) {
            imgY = 0;
        }

        if (imgY + size > IMG_SIZE) {
            imgY = IMG_SIZE - size;
        }

        SVG.viewBox.baseVal.width = size;
        SVG.viewBox.baseVal.height = size;
        SVG.viewBox.baseVal.x = imgX;
        SVG.viewBox.baseVal.y = imgY;

        CONFIG.viewport.x = imgX;
        CONFIG.viewport.y = imgY;
        SOCKET.write('VIEWPORT', CONFIG.viewport)
    }

    function svgCenter() {
        return {
            x: SVG.viewBox.baseVal.x + SVG.viewBox.baseVal.width / 2,
            y: SVG.viewBox.baseVal.y + SVG.viewBox.baseVal.height / 2
        }
    }


    function onMessage(msg) {
        switch (msg.type) {
            case 'CONFIGURATION':
                initSession(JSON.parse(msg.payload));
                break;
            case 'ERROR':
                LOGGER.error(msg.payload);
                BUTTON_START.disabled = false;
                BUTTON_STOP.disabled = true;
                break;
            case 'LOG':
                LOGGER.log(msg.payload);
                break;
            case 'END':
                LOGGER.log("Session stopped");
                BUTTON_START.disabled = false;
                BUTTON_STOP.disabled = true;
                break;
            case 'STATE':
                drawState(JSON.parse(msg.payload));
                setTimeout(function () {
                    SOCKET.write("NEXT", INTERVAL.value);
                }, DELAY.value)
                break;
        }
    }

    function initSession(conf) {
        const rules = conf.rules;
        Object.assign(CONFIG, conf)
        delete CONFIG.rules;


        // Population slider
        createSlider('population-slider', {
            min: 1,
            max: CONFIG.maxPopulation,
            value: CONFIG.population,
            onValue: function (v) {
                CONFIG.population = v;
            }
        })

        // Working percent slider
        createSlider('working-percent-slider', {
            min: 0,
            max: 100,
            value: CONFIG.workingPercent,
            onValue: function (v) {
                CONFIG.workingPercent = v;
            }
        })

        // Working percent slider
        createSlider('non-working-stay-home', {
            min: 0,
            max: 100,
            value: CONFIG.leisureAtHomePercent,
            onValue: function (v) {
                CONFIG.leisureAtHomePercent = v;
                SOCKET.write("UPDATE", CONFIG);
            }
        })

        // Speed slider
        createSlider('speed-slider', {
            min: 5,
            max: 50,
            value: CONFIG.commuteSpeed,
            onValue: function (v) {
                CONFIG.commuteSpeed = v;
                SOCKET.write("UPDATE", CONFIG);
            }
        })

        // Init resolution
        RESOLUTION.value = CONFIG.viewport.resolution;
        RESOLUTION.onchange = () => {
            CONFIG.viewport.resolution = RESOLUTION.value;
            SOCKET.write('VIEWPORT', CONFIG.viewport);
        }

        // Init rule monitor
        removeChildren(RULES);
        rules.forEach(r => {
            const tr = document.createElement('tr');
            const td1 = document.createElement('td');
            const td2 = document.createElement('td');
            td1.innerText = r.name;
            td2.id = r.id;
            td2.innerText = '0';
            tr.appendChild(td1);
            tr.appendChild(td2);
            RULES.appendChild(tr);
        })
    }

    function drawState(state) {
        // Set time
        document.getElementById('chart-time').innerHTML = state['time'];

        // Clearing SVG layers
        const layersGroup = document.getElementById('svg-layers');
        if (state['reset']) {
            removeChildren(layersGroup);
        }

        drawSummary(state['total']);
        drawRuleActivity(state['activity']);


        let cellSize = state['cellSize'];
        // clear current status
        let layers = state['layers'];
        for (const key in layers) {
            if (Object.hasOwn(layers, key)) {
                let svgLayer = document.getElementById(key);

                if (!svgLayer) {
                    // Create a new one
                    svgLayer = document.createElementNS(SVG_NS, 'g');
                    svgLayer.setAttribute('id', key);
                    svgLayer.classList.add(key);
                    layersGroup.appendChild(svgLayer);
                }

                let cells = layers[key];
                for (let i = 0; i < cells.length; i++) {
                    const cell = cells[i];
                    const cellId = cell['id'];
                    let rect = document.getElementById(cellId);
                    if (!rect) {
                        rect = document.createElementNS(SVG_NS, 'rect');
                        rect.setAttribute('id', cellId);
                        rect.setAttribute("x", cell.x);
                        rect.setAttribute("y", cell.y);
                        rect.setAttribute("width", cellSize);
                        rect.setAttribute("height", cellSize);
                        svgLayer.appendChild(rect);
                    }
                    rect.setAttribute("fill-opacity", cell.opacity);
                }
            }
        }
    }

    function drawRuleActivity(map) {
        for (const ruleId in map) {
            if (Object.hasOwn(map, ruleId)) {
                const cnt = document.getElementById(ruleId);
                cnt.innerText = map[ruleId].toLocaleString();
            }
        }
    }

    function drawSummary(data) {
        // Draw pie chart
        const pieGroup = document.getElementById('chart-pie');
        for (const key in data) {
            if (Object.hasOwn(data, key)) {
                const e = document.getElementById('legend-' + key);
                if (e) {
                    const p = Math.round(data[key] * 100);
                    e.innerHTML = p.toString() + '%';
                }
            }
        }

        removeChildren(pieGroup);
        const radius = 50;
        let currentAngle = Math.PI / 2;
        let currentX = 0;
        let currentY = -radius;
        for (const key in data) {
            if (Object.hasOwn(data, key)) {
                let val = data[key];
                val = val >= 1.0 ? 0.9999999 : val; // Necessary to draw an arc
                const angle = 2.0 * Math.PI * val;
                const largeArc = angle > Math.PI ? ' 1 ' : ' 0 ';

                let path = 'M 0 0 ' + currentX + ' ' + currentY + ' A ' + radius + ' ' + radius + ' 0 ' + largeArc + ' 1 ';
                currentAngle = currentAngle - angle;
                currentX = radius * Math.cos(currentAngle);
                currentY = -radius * Math.sin(currentAngle);
                path = path + ' ' + currentX + ' ' + currentY + ' Z';
                const pieSector = document.createElementNS(SVG_NS, 'path');
                pieSector.setAttribute('d', path);
                pieSector.setAttribute('class', key);
                pieGroup.appendChild(pieSector);
            }
        }
    }

    return {
        sessionStart: function () {
            clearChildren('clearable')
            LOGGER.log("Starting a new session...")
            SOCKET.write('START', CONFIG)
            BUTTON_START.disabled = true;
            BUTTON_STOP.disabled = false;
        },

        sessionStop: function () {
            SOCKET.write('STOP');
        }
    }
}
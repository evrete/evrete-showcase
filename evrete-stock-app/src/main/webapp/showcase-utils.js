export function createWSConnection(path, conf) {
    const onOpen = conf['onopen'] || conf['onOpen'];
    const onError = conf['onerror'] || conf['onError'];
    const onClose = conf['onclose'] || conf['onClose'];
    const onMessage = conf['onmessage'] || conf['onMessage'];

    if ('WebSocket' in window || 'MozWebSocket' in window) {
        const webSocket = new WebSocket('ws://' + location.host + path);

        if (onOpen) {
            webSocket.onopen = onOpen
        }

        if (onClose) {
            webSocket.onclose = onClose;
        }

        webSocket.onmessage = function (m) {
            onMessage(JSON.parse(m.data));
        };

        if (onError) {
            webSocket.onerror = onError;
        } else {
            webSocket.onerror = function (err) {
                console.error(err);
            }
        }

        return {
            write: function (type, obj) {
                if (obj) {
                    if (typeof obj === 'object') {
                        webSocket.send(JSON.stringify({type: type, payload: JSON.stringify(obj)}))
                    } else {
                        webSocket.send(JSON.stringify({type: type, payload: obj}))
                    }
                } else {
                    webSocket.send(JSON.stringify({type: type}))
                }
            }
        }
    } else {
        const err = 'Web sockets are not supported by the browser';
        if (onError) {
            onError(err);
        }
        console.error(err);
        return {
            write: function () {
                if (onError) {
                    onError(err);
                }
            }
        }
    }
}

export function forEachByClassName(styleClass, func) {
    const collection = document.getElementsByClassName(styleClass);
    for (let i = 0; i < collection.length; i++) {
        func(collection.item(i));
    }
}


export function clearChildren(styleClass) {
    forEachByClassName(styleClass, el => removeChildren(el));
}

export function removeChildren(el) {
    if (el) {
        while (el.children.length > 0) {
            el.removeChild(el.children.item(0));
        }
    }
}


export function createSlider(parentId, options) {
    const parent = document.getElementById(parentId);
    if (parent) {
        const onValue = options['onValue'];
        removeChildren(parent);
        const wrapper = document.createElement('div');
        wrapper.classList.add('row')
        wrapper.classList.add('justify-content-center')
        wrapper.classList.add('justify-content-sm-start')
        wrapper.classList.add('g-1')
        const col_l = document.createElement('div');
        col_l.classList.add('col')
        col_l.classList.add('small')
        const col_r = document.createElement('div');
        col_r.classList.add('col-auto')
        col_r.classList.add('text-center')
        col_r.classList.add('font-monospace')
        col_r.classList.add('px-1')
        col_r.classList.add('small')
        col_r.classList.add('fw-light')

        const i = document.createElement('input');
        const v = document.createElement('span');
        v.classList.add('small')

        i.id = parentId + '_i';
        i.type = 'range';
        i.min = options.min;
        i.max = options.max;
        i.value = options.value;
        if(options.step) {
            i.step = options.step;
        }
        i.classList.add('form-range');
        if (onValue) {
            i.onchange = function () {
                onValue(i.value);
            }
        }

        i.oninput = function () {
            v.innerText = i.value;
        }
        v.innerText = options.value;

        col_r.appendChild(v);
        col_l.appendChild(i);
        wrapper.appendChild(col_l);
        wrapper.appendChild(col_r);
        parent.appendChild(wrapper);
    } else {
        console.error('No such element', parentId);
    }
}

export function createLogger(id) {
    const el = document.getElementById(id);
    if (!el) {
        console.warn("No such id:", id)
    }


    return {
        log: function (msg) {
            if (el) {
                const item = document.createElement("li");
                item.innerText = msg;
                el.append(item);
            }
        },
        error: function (msg) {
            if (el) {
                const item = document.createElement("li");
                item.className = 'ERROR';
                item.innerText = msg;
                el.append(item);
            }
        }
    }
}
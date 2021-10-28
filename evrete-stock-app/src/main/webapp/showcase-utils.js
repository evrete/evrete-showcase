export function createWSConnection(path, onMessage, onError, onClose) {
    if ('WebSocket' in window || 'MozWebSocket' in window) {
        const webSocket = new WebSocket('ws://' + location.host + path);

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

        if (onClose) {
            webSocket.onclose = onClose;
        }
        return {
            write: function (type, obj) {
                if(obj) {
                    if(typeof obj === 'object') {
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


export function createLogger(id) {
    const el = document.getElementById(id);
    if (!el){
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
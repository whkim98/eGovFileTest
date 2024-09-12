export const AmFileUploadHTML5=(function(){

    /**
     * 파일 삭제, 업로드 순서 변경을 위해서는 반드시 파일추가(add) 콜백함수를 통해서 element에 bind 처리 필요.
     * AmFileUploadHTML5.bind(key, bindElement);
     * AmFileUploadHTML5.deleteFile(bindElement);
     * AmFileUploadHTML5.moveFile(bindElement, destinationIndex);
     */

    'use strict'

    let _submitting = false;
    let _fileMap = new Map(), _uploadedFileMap = new Map();
    let	_formData, _initData, _xhr;

    function init(params)
    {
        if(!Object.assign)
        {
            alert('AmFileUploadHTML5 is not supported by your browser.\n\nBrowser compatibility : Chrome, Firefox, Opera, Edge');
            if(window.opener && window.opener !== window)
                self.close();
            return;
        }

        _initData = Object.assign(
            {},
            {
                'debug'				: false,
                'formElement'		: undefined,
                'fileElement'		: undefined,
                'dropZoneElement'	: undefined,
                // 'uniqueFile'		: true,
                'allowFileType'		: [],
                'allowFileExtension': [],
                'uploadUrl'			: undefined,
                'sequentialUploads'	: false,
                'async'				: true,
                'user'				: null,
                'password'			: null,
                'responseType'		: "text",		// arraybuffer, blob, document, json, text(default)
                // 'timeout'			: 0,			// milliseconds, 0(default : no timeout)
                'withCredentials'	: true,			// default is false
                'overrideMimeType'	: undefined,	// overriding the server's stated type for the data being received
                'requestHeader'		: {}, //{"Content-Type" : "multipart/form-data; charset=utf-8; boundary=" + Math.random().toString().substr(2)},

                // fileElement Event
                'dragover'			: function(){},
                'dragleave'			: function(){},
                'drop'				: function(){},
                'add'				: function(){},

                'success'			: function(){},

                // XMLHttpRequest.upload Event
                'loadstart'			: function(){},
                'progress'			: function(){},
                'abort'				: function(){},
                'error'				: function(){},
                'load'				: function(){},
                'timeout'			: function(){},
                'loadend'			: function(){}
            },
            params
        );
        _initData.debug&&console.log("AmFileUploadHTML5 _initData", _initData);

        if(!_initData.formElement && !_initData.fileElement)
            return;

        _initData.fileElement = _initData.fileElement ? _initData.fileElement : _initData.formElement.querySelector("input[type='file']");
        _initData.fileElement.accept = '.'+_initData.allowFileExtension.join(',.');

        let inAllowFile = function(target, arr, caseSensitive){
            let match = false;
            arr.some(function(val){
                match = caseSensitive ? target===val : target.toUpperCase()===val.toUpperCase();
                return match;
            })
            return match;
        };

        let setFiles = function(addFiles){
            if(_submitting)
                return false;
            let attachFileMap = new Map();
            addFiles.forEach(function(addFile){
                let sameFile;
                for(let file of _fileMap.values()){
                    sameFile = (addFile.name===file.name && addFile.size===file.size);
                    if(sameFile)
                        break;
                }

                // if(!(_initData.uniqueFile))
                if(!sameFile)	// name, size compare
                {
                    let allow = true;
                    if(_initData.allowFileType.length>0)
                        allow = inAllowFile(addFile.type, _initData.allowFileType, false);
                    if(_initData.allowFileExtension.length>0)
                        allow = inAllowFile(addFile.name.substr(addFile.name.lastIndexOf('.')+1), _initData.allowFileExtension, false);

                    if(allow){
                        let uuid = _generateUUID();
                        _fileMap.set(uuid, addFile);
                        attachFileMap.set(uuid, addFile);
                    }
                }
            });

            if( attachFileMap.size>0 )
                _initData.add(attachFileMap, _fileMap);
        };

        _initData.fileElement.onchange = function(event){
            _initData.debug&&console.log("AmFileUploadHTML5 fileElement.onchange", event);
            if(_submitting)
                return false;
            setFiles(Array.from(this.files));
        };

        if(_initData.dropZoneElement){
            window.ondragover = window.ondrop = function(event){
                event.preventDefault();
            };
            _initData.dropZoneElement.ondragover = function(event){
                _initData.debug&&console.log("AmFileUploadHTML5 dropZoneElement.ondragover", event);
                event.preventDefault();
                if(_submitting)
                    return false;
                _initData.dragover(event);
            };
            _initData.dropZoneElement.ondragleave = function(event){
                _initData.debug&&console.log("AmFileUploadHTML5 dropZoneElement.ondragleave", event);
                event.preventDefault();
                if(_submitting)
                    return false;
                _initData.dragleave(event);
            };
            _initData.dropZoneElement.ondrop = function(event){
                _initData.debug&&console.log("AmFileUploadHTML5 dropZoneElement.ondrop", event);
                event.preventDefault();
                if(_submitting)
                    return false;
                setFiles(Array.from(event.dataTransfer.files));	// e.dataTransfer.files or e.dataTransfer.items
                _initData.drop(event);
            };
        }

        return true;
    }

    function submit(addParams)
    {
        if(_submitting)
            return false;

        if(_fileMap.size==_uploadedFileMap.size)
            return false;

        let sendFileMap = new Map();
        _fileMap.forEach(function(file, key){
            if(!_uploadedFileMap.has(key))
                sendFileMap.set(key, file);
        });

        if(_initData.sequentialUploads)
            sendFileMap = new Map([sendFileMap.entries().next().value]);

        return _send(addParams, sendFileMap);
    }

    function _send(addParams, sendFileMap)
    {
        if(_submitting)
            return false;

        _formData = new FormData(_initData.formElement);
        if(addParams){
            Object.keys(addParams).forEach(function(key){
                _formData.append(key, addParams[key]);
            });
        }

        _xhr = new XMLHttpRequest()	// _xhr.UNSENT
        _xhr.onreadystatechange = function(event){
            // UNSENT(0), OPENED(1), HEADERS_RECEIVED(2), LOADING(3), DONE(4)
            if(_xhr.readyState===XMLHttpRequest.HEADERS_RECEIVED)
                _initData.debug&&console.log("AmFileUploadHTML5 XMLHttpRequest readystatechange HEADERS_RECEIVED", _xhr.getAllResponseHeaders());
            if(_xhr.readyState===XMLHttpRequest.LOADING)
                _initData.debug&&console.log("AmFileUploadHTML5 XMLHttpRequest readystatechange LOADING", _xhr);
            if(_xhr.readyState===XMLHttpRequest.DONE){
                if(_xhr.status===200 || _xhr.status===201){
                    _initData.debug&&console.log("AmFileUploadHTML5 XMLHttpRequest readystatechange DONE "+_xhr.status, _xhr);
                    _submitting = false;

                    let arrUploadedFile = _formData.getAll(_getFileElementName());
                    arrUploadedFile.forEach(function(uploadedFile){
                        _uploadedFileMap.set(_keyOfFile(uploadedFile), uploadedFile);
                    });

                    if(_initData.sequentialUploads && _uploadedFileMap.size<_fileMap.size)
                        setTimeout(function(){
                            AmFileUploadHTML5.submit(addParams);
                        },10);

                    _initData.success(_xhr.response, _xhr, _fileMap.size-_uploadedFileMap.size);
                }
                else{
                    _initData.debug&&console.log("AmFileUploadHTML5 XMLHttpRequest readystatechange DONE"+_xhr.status, _xhr);
                    // TO DO : 3xx 처리 필요?...
                }
            }
        };
        _xhr.upload.onloadstart = function(event){
            _initData.debug&&console.log("AmFileUploadHTML5 XMLHttpRequest upload.loadstart", event);
            _initData.loadstart(event);
        };
        _xhr.upload.onprogress = function(event){	// _xhr.LOADING
            _initData.debug&&console.log("AmFileUploadHTML5 XMLHttpRequest upload.progress", event);
            let key;
            if(_initData.sequentialUploads&&event.lengthComputable)
                key = sendFileMap.keys().next().value;
            _initData.progress(event, _getBindElement(key));
        };
        _xhr.upload.onabort = function(event){
            _initData.debug&&console.log("AmFileUploadHTML5 XMLHttpRequest upload.abort", event);
            _submitting = false;
            _initData.abort(event);
        };
        _xhr.upload.onerror = function(event){
            _initData.debug&&console.log("AmFileUploadHTML5 XMLHttpRequest upload.error", event);
            _submitting = false;
            _initData.error(event);
        };
        _xhr.upload.onload = function(event){	// _xhr.DONE
            _initData.debug&&console.log("AmFileUploadHTML5 XMLHttpRequest upload.load", event);
            _submitting = false;
            _initData.load(event);
        };
        _xhr.upload.ontimeout = function(event){
            _initData.debug&&console.log("AmFileUploadHTML5 XMLHttpRequest upload.timeout", event);
            _submitting = false;
            _initData.timeout(event);
        };
        _xhr.upload.onloadend = function(event){
            _initData.debug&&console.log("AmFileUploadHTML5 XMLHttpRequest upload.loadend", event);
            _submitting = false;
            _initData.loadend(event);
        };

        sendFileMap.forEach(function(file){
            _formData.append(_getFileElementName(), file);
        });

        if(_initData.overrideMimeType) _xhr.overrideMimeType(_initData.overrideMimeType);
        _xhr.responseType = _initData.responseType;
        _xhr.timeout = _initData.timeout;
        _xhr.withCredentials = _initData.withCredentials;
        _xhr.open("POST", _initData.uploadUrl, _initData.async, _initData.user, _initData.password);	// _xhr.OPENED
        Object.keys(_initData.requestHeader).forEach(function(key){
            _xhr.setRequestHeader(key, _initData.requestHeader[key]);
        });
        _submitting = true;
        _xhr.send(_formData);
    }

    function abort()
    {
        if(_submitting)
        {
            setTimeout(function(){
                _initData.debug&&console.log("AmFileUploadHTML5 XMLHttpRequest abort");
                _xhr.abort();
            }, 10);
        }
    }

    function bind(key, element)
    {
        if(!key || !element || typeof(element.tagName)!="string")
            return false;
        element.dataset.uuid = key;
        return true;
    }

    function _keyOfFile(targetFile)
    {
        for(let [key, file] of _fileMap.entries()){
            if(file===targetFile)
                return key;
        }
    }

    function _getFileElementName()
    {
        let fileElementName = _initData.fileElement.name ? _initData.fileElement.name:"file";
        return _initData.sequentialUploads ? fileElementName:fileElementName+"[]";
    }

    function _getBindElement(key)
    {
        return document.querySelector("[data-uuid='"+key+"']");
    }

    function deleteFile(bindElement)
    {
        if(_submitting)
            return false;

        _uploadedFileMap.delete(bindElement.dataset.uuid);
        var result = _fileMap.delete(bindElement.dataset.uuid);

        _initData.debug&&console.log("AmFileUploadHTML5 deleteFile", _fileMap);
        return result;
    }

    function moveFile(bindElement, destinationIndex)
    {
        if(!bindElement || typeof(bindElement.tagName)!="string" || destinationIndex<0 || destinationIndex>(_fileMap.size-1))
            return false;

        let targetKey = bindElement.dataset.uuid;
        if(!_fileMap.has(targetKey))
            return false;

        let arrEntrie = Array.from(_fileMap);
        let targetIndex;
        arrEntrie.some(function(entrie, index){
            if(entrie[0]==targetKey)
            {
                targetIndex = index;
                return true;
            }
        });

        if(targetIndex<0)
            return false;

        let targetEntrie = arrEntrie.splice(targetIndex, 1)[0];
        if(destinationIndex==0)
        {
            arrEntrie.unshift(targetEntrie);
        }
        else if(destinationIndex==arrEntrie.length)
        {
            arrEntrie.push(targetEntrie);
        }
        else
        {
            let arrFrontEntrie = arrEntrie.slice(0, destinationIndex);
            let arrBackEntrie = arrEntrie.slice(arrFrontEntrie.length);
            arrEntrie = [].concat(arrFrontEntrie, [targetEntrie], arrBackEntrie);
        }

        _fileMap = new Map(arrEntrie);
        _initData.debug&&console.log("AmFileUploadHTML5 moveFile", _fileMap);
        return true;
    }

    function requestHead(url)
    {
        if(!url) return false;
        _xhr = new XMLHttpRequest();
        _xhr.open("HEAD", url);
        _xhr.onload = function(event){console.log(event);};
        _xhr.send();
    }

    function requestOption(url)
    {
        if(!url) return false;
        _xhr = new XMLHttpRequest();
        _xhr.open("OPTION", url);
        _xhr.onload = function(event){console.log(event);};
        _xhr.send();
    }

    // https://gist.github.com/gordonbrander/2230317#gistcomment-1618310
    function _generateUUID()
    {
        let chr4 = function(){
            return Math.random().toString(16).slice(-4);
        };
        return chr4() + chr4() +
            '-' + chr4() +
            '-' + chr4() +
            '-' + chr4() +
            '-' + chr4() + chr4() + chr4();
    }

    return {
        'init'				: init,
        'submit'			: submit,
        'abort'				: abort,
        'bind'				: bind,
        'deleteFile'		: deleteFile,
        'moveFile'			: moveFile,
        'requestHead'		: requestHead,
        'requestOption'		: requestOption,
        'getFileMap'		: function(){return _fileMap;},
        'getUploadedFileMap': function(){return _uploadedFileMap;},
        get submitting(){return _submitting;},
        get fileCount(){return _fileMap.size;}
    };
})();
window.AmFileUploadHTML5 = AmFileUploadHTML5;

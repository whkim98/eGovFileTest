import {AMCookie} from '../cookie.js'

export const AmFileUpload=(function(){
    "use strict";

    // 메시지 박스
    function message_box(msg)
    {
        if( typeof AppBridgeMsg==='object' && AppBridgeMsg.hasOwnProperty("Alert") && typeof AppBridgeMsg.Alert==='function' )
        {
            let title = '알림';
            if( typeof AppBridgeMsgInfo==='object' && AppBridgeMsgInfo.hasOwnProperty("service_name") && AppBridgeMsgInfo.service_name!="" )
                title = AppBridgeMsgInfo.service_name;

            AppBridgeMsg.Alert({"title": title, "msg": msg});
        }
        else
        {
            alert(msg);
        }
    }

    // 이미지 크기를 얻는다.
    function verify_image_dimensions(objArgument,uploadFile,data)
    {
        let img = new Image();
        let _URL = window.URL || window.webkitURL;
        img.src = _URL.createObjectURL( uploadFile );
        img.onload = function() {
            let width  = img.naturalWidth;
            let height = img.naturalHeight;
            if( width<=0 || height<=0 )
                return;

            const _fn_verify = function(dimensions_range,width,height){
                // width 검사
                if( dimensions_range.hasOwnProperty("min_width") )
                {
                    if( width < dimensions_range.min_width )
                        return false;
                }
                if( dimensions_range.hasOwnProperty("max_width") )
                {
                    if( width > dimensions_range.max_width )
                        return false;
                }

                // height 검사
                if( dimensions_range.hasOwnProperty("min_height") )
                {
                    if( height < dimensions_range.min_height )
                        return false;
                }
                if( dimensions_range.hasOwnProperty("max_height") )
                {
                    if( height > dimensions_range.max_height )
                        return false;
                }

                return true;
            };

            if( !Array.isArray(objArgument.file_upload_filter.dimensions.range) )
                objArgument.file_upload_filter.dimensions.range = [objArgument.file_upload_filter.dimensions.range];

            let range_result = false;
            for( let key in objArgument.file_upload_filter.dimensions.range )
            {
                if( _fn_verify(objArgument.file_upload_filter.dimensions.range[key],width,height) )
                {
                    range_result = true
                    break;
                }
            }

            if( !range_result )
            {
                message_box(objArgument.file_upload_filter.dimensions.error_msg);
                if( typeof objArgument.fn_reset==='function' )
                    objArgument.fn_reset(data, objArgument.user_data);

                //m_objForm		= null; // 초기화하면 안된다.
                //m_objFile		= null; // 초기화하면 안된다.
                m_objFileUpload = null;
            }

            _URL.revokeObjectURL( img.src );
        };
    }

    let m_objForm		= null;
    let m_objFile		= null;
    let m_objFileUpload	= null;
    let m_is_on_submit	= false;

    //let objArgument = {
    //					'objForm'				: $("#detail_form"),
    //					'server_url'			: EnvLocal.content_upload_server_url + "lib_service/bbs_file/bbs_file_upload.php",	// objForm.action 으로 대체 가능
    //					'fn_add'				: add_cb,		// 파일 선택
    //					'fn_reset'				: fn_reset,		// 초기화(일반 함수)
    //					'fn_submit'				: submit_cb,	// [전송] 클릭
    //					'fn_start'				: start_cb,		// 인디게이터 표시 등에 사용
    //					'fn_progress'			: progress_cb,	// 프로그레스바 표시 등에 사용
    //					'fn_success'			: success_cb,	// 파일 전송 완료
    //					'fn_error'				: error_cb,		// 서버 통신 오류
    //					'file_upload_filter'	: {
    //												// 확장자 필터링
    //												'extension'	: {
    //																//'accept'			: '.png,.jpg,.jpeg,.gif',					// type="file" 에서 사용
    //																// 적용 순서: ext_reg_exp → ext_list
    //																'ext_reg_exp'		: '^.*\.(png|jpe?g|gif)$',					// 확장자 필터링 정규식 (1순위 적용)
    //																'ext_list'			: 'png|jpe?g|gif',							// 확장자 (2순위 적용)
    //																'error_msg'			: 'png, jpg, gif 파일만 업로드 가능합니다.'	// 서버 에러 메시지
    //															},
    //												// 파일 크기 필터링
    //												'filesize'	: {
    //																'upload_max_filesize'	: 1024000,	// 단위: 바이트
    //																'error_msg'				: '업로드 파일 용량은 1MB를 초과할 수 없습니다.'
    //															},
    //												// 이미지 크기 필터링
    //												'dimensions': {
    //																'range'	: [
    //																			{'min_width':0, 'max_width':1000, 'min_height':0, 'max_height':1000},
    //																			{'min_width':0, 'max_width':1000, 'min_height':0, 'max_height':1000}
    //																			],
    //																'error_msg'	: '업로드할 이미지 파일의 크기는 (0~1000)x(0~1000)이어야 합니다.'
    //															}
    //												'dimensions': {
    //																'range'	: {
    //																			'min_width'		: 0,
    //																			'max_width'		: 1000,
    //																			'min_height'	: 0,
    //																			'max_height'	: 1000
    //																			},
    //																'error_msg'	: '업로드할 이미지 파일의 크기는 (0~1000)x(0~1000)이어야 합니다.'
    //															}
    //											}
    //					//'user_data			: ""
    //				};
    function initialize(objArgument)
    {
        if( !(objArgument.objForm instanceof jQuery) )
            objArgument.objForm = $(objArgument.objForm);

        // form 태그
        if( !objArgument.objForm.is('form') )
        {
            message_box("Form object error!!!");
            return;
        }

        // file 개체
        let objFile = $("input[type=file]", objArgument.objForm);
        if( typeof objFile!=='object' )
        {
            message_box("File object error!!!");
            return;
        }

        m_objFile = objFile;

        if( typeof objArgument.server_url==='undefined' )
            objArgument.server_url = objArgument.objForm.attr("action");

        // 업로드 URL 검사
        if( objArgument.server_url=="")
        {
            message_box("Server upload url error!!!");
            return;
        }

        m_objForm = objArgument.objForm;

        objFile.fileupload(
            {
                // 1. AJAX Options
                // - url
                // - type : Can be "POST", "PUT" or "PATCH" and defaults to "POST"
                // - dataType : "json" by default.
                'url'				: objArgument.server_url,
                'type'				: 'POST',
                'dataType'			: 'json',
                'progressInterval'	: 100,		// 단위: 밀리초
                'replaceFileInput'	: false,	// 만약 fileUpload 플러그인을 업로드 용도가 아닌 파일의 유효성 검사를 체크하기 위해서 사용한다면 이 옵션을 false로 하여 사용한다.
                'add':
                    function(e, data)
                    {
                        let uploadFile = data.files[0];

                        // 파일 필터링
                        if( typeof objArgument.file_upload_filter==='object' && !$.isEmptyObject(objArgument.file_upload_filter) )
                        {
                            // 1. 확장자 필터링
                            if( typeof objArgument.file_upload_filter.extension==='object' )
                            {
                                //if( !(/.png|.jpe?g|.gif/i).test(uploadFile.name) )
                                let ext_reg_exp = '';
                                if( typeof objArgument.file_upload_filter.extension.ext_reg_exp==='string' && objArgument.file_upload_filter.extension.ext_reg_exp!="" )
                                    ext_reg_exp = objArgument.file_upload_filter.extension.ext_reg_exp;
                                else if( typeof objArgument.file_upload_filter.extension.ext_list==='string' && objArgument.file_upload_filter.extension.ext_list!="" )
                                    ext_reg_exp = '^.*\.('+objArgument.file_upload_filter.extension.ext_list+')$';

                                if( ext_reg_exp!="" )
                                {
                                    let extRegExp = new RegExp(ext_reg_exp, "i");
                                    let extArray = extRegExp.exec(uploadFile.name);
                                    if( extArray==null )
                                    {
                                        message_box(objArgument.file_upload_filter.extension.error_msg);
                                        if( typeof objArgument.fn_reset==='function' )
                                            objArgument.fn_reset(data, objArgument.user_data);

                                        //m_objForm		= null; // 초기화하면 안된다.
                                        //m_objFile		= null; // 초기화하면 안된다.
                                        m_objFileUpload	= null;
                                        return;
                                    }
                                }
                            }

                            // 2. 파일 용량 검사
                            if( typeof objArgument.file_upload_filter.filesize==='object' )
                            {
                                // 숫자로 변환
                                if( typeof objArgument.file_upload_filter.filesize.upload_max_filesize!=='number' )
                                    objArgument.file_upload_filter.filesize.upload_max_filesize = parseInt(objArgument.file_upload_filter.filesize.upload_max_filesize,10);

                                // 용량 검사
                                if( objArgument.file_upload_filter.filesize.upload_max_filesize > 0 )
                                {
                                    if( uploadFile.size > objArgument.file_upload_filter.filesize.upload_max_filesize )
                                    {
                                        message_box(objArgument.file_upload_filter.filesize.error_msg);
                                        if( typeof objArgument.fn_reset==='function' )
                                            objArgument.fn_reset(data, objArgument.user_data);

                                        //m_objForm		= null; // 초기화하면 안된다.
                                        //m_objFile		= null; // 초기화하면 안된다.
                                        m_objFileUpload = null;
                                        return;
                                    }
                                }
                            }

                            // 3. 이미지 크기 검사
                            if( typeof objArgument.file_upload_filter.dimensions==='object' )
                            {
                                // 이미지 크기는 비동기로 동작한다.
                                verify_image_dimensions(objArgument,uploadFile,data);
                            }

                            // 4. 동영상 파일 포맷 검사
                            // 참고: http://stackoverflow.com/questions/25838472/how-to-get-video-height-and-width-from-a-mp4-url-with-client-side-only
                            //if( typeof objArgument.file_upload_filter.mp4format==='object' )
                            //{
                            //	// 동영상 크기는 비동기로 동작한다.
                            //	verify_mp4_dimensions(objArgument,uploadFile,data);
                            //}
                        }

                        m_objFileUpload = data;
                        // 파일 추가와 동시에 전송하지 않기 위하여 객체를 전달
                        // data.submit();
                        if( typeof objArgument.fn_add==='function' )
                            objArgument.fn_add(data, objArgument.user_data);
                    },
                'submit':
                    function(e,data)
                    {
                        if( typeof objArgument.fn_submit === 'function' )
                        {
                            let ret_submit = objArgument.fn_submit(data, objArgument.user_data);
                            if( !ret_submit )
                                return false;
                        }

                        // 여기까지 오면 전송이 진행된다.

                        // response_type 추가
                        let objResponseType = objArgument.objForm.find('input[name=response_type]');
                        if( objResponseType.length > 0 )
                            objResponseType.remove();

                        let objHidden = $('<input>').attr({
                            'type'	: 'hidden',
                            'name'	: 'response_type',
                            'value'	: 'json'
                        });
                        objArgument.objForm.prepend(objHidden);

                        // file_pre_upload_option 추가
                        //if( typeof objArgument.file_pre_upload_option==='object' && !$.isEmptyObject(objArgument.file_pre_upload_option) )
                        //{
                        //	let objFileFilter = $('<input>').attr({
                        //										'type'	: 'hidden',
                        //										'name'	: 'file_pre_upload_option',
                        //										'value'	: JSON.stringify(objArgument.file_pre_upload_option)
                        //					});
                        //	objArgument.objForm.prepend(objFileFilter);
                        //}

                        // file_upload_filter 추가
                        if( typeof objArgument.file_upload_filter==='object' && !$.isEmptyObject(objArgument.file_upload_filter) )
                        {
                            let objFileFilter = $('<input>').attr({
                                'type'	: 'hidden',
                                'name'	: 'file_upload_filter',
                                'value'	: JSON.stringify(objArgument.file_upload_filter)
                            });
                            objArgument.objForm.prepend(objFileFilter);
                        }

                        // file_uploader_key 추가
                        if( typeof objArgument.file_uploader_key==='string' && objArgument.file_uploader_key!='' )
                        {
                            let objFileFilter = $('<input>').attr({
                                'type'	: 'hidden',
                                'name'	: 'file_uploader_key',
                                'value'	: objArgument.file_uploader_key
                            });
                            objArgument.objForm.prepend(objFileFilter);
                        }

                        // session_name → session_id 추가
                        if( typeof objArgument.session_name==='string' && objArgument.session_name!='' )
                        {
                            let session_id = AMCookie.get_session(objArgument.session_name);
                            let objFileFilter = $('<input>').attr({
                                'type'	: 'hidden',
                                'name'	: 'session_id',
                                'value'	: session_id
                            });
                            objArgument.objForm.prepend(objFileFilter);
                        }

                        return true;
                    },
                'start':
                    function(e,data)
                    {
                        if( typeof objArgument.fn_start === 'function' )
                            objArgument.fn_start(data, objArgument.user_data);

                        m_is_on_submit = true;
                    },
                'progressall':
                    function(e,data)
                    {
                        if( typeof objArgument.fn_progress === 'function' )
                            objArgument.fn_progress(data, objArgument.user_data);
                    },
                'done':
                    function (e, data)
                    {
                        if( typeof objArgument.fn_success === 'function' )
                            objArgument.fn_success(data, objArgument.user_data);

                        m_is_on_submit = false;
                        //m_objFileUpload = null;

                        // 초기화
                        // input file 유형에 대해서 초기화 하는 작업을 진행하지 않으면 재호출 시 동작하지 않음.
                        $(objFile).val("");	// == $("#uploadfile").val("");
                        // objArgument.fn_reset(data, objArgument.user_data);
                    },
                'fail':
                    function(e, data)
                    {
                        message_box('서버로 파일 전송에 실패했습니다.');
                        if( typeof objArgument.fn_error === 'function' )
                            objArgument.fn_error(data, objArgument.user_data);

                        m_is_on_submit = false;
                        //m_objFileUpload = null;

                        // message_box(data.errorThrown);
                        // message_box(data.textStatus);
                        // message_box(data.jqXHR);
                    }
            }
        );
    }

    function is_on_submit()
    {
        return m_is_on_submit;
    }

    // 파일 전송
    function submit()
    {
        // 파일 추가
        if( m_objFileUpload==null )
        {
            message_box("파일을 선택해 주세요.");
            return;
        }

        // 파일 개체
        // if( m_objFile.val()=='' )
        // {
        // 	message_box("파일을 선택해 주세요.");
        // 	m_objFile.focus();
        // 	return;
        // }

        if( m_objFileUpload.files.length<=0 )
        {
            message_box("파일을 선택해 주세요.");
            return;
        }

        // 업로드 중이면 반환
        if( is_on_submit() )
            return;

        // 파일 업로드 수행
        m_objFileUpload.submit();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    return {
        'message_box'	:	message_box,
        'initialize'	:	initialize,
        'is_on_submit'	:	is_on_submit,
        'submit'		:	submit
    };
})();
window.AmFileUpload = AmFileUpload;

export const AMCookie=(function(){
    "use strict";

    // public method for encoding
    function getCookieVal_x(offset)
    {
        let endstr = document.cookie.indexOf(";", offset);
        if (endstr == -1)
            endstr = document.cookie.length;
        return unescape(document.cookie.substring(offset, endstr));
    }

    function get_x(name)
    {
        let arg = name + "=";
        let alen = arg.length;
        let clen = document.cookie.length;
        let i = 0;
        while (i < clen)
        {
            let j = i + alen;
            if (document.cookie.substring(i, j) == arg)
                return getCookieVal_x (j);
            i = document.cookie.indexOf(" ", i) + 1;
            if (i == 0)
                break;
        } //while close
        return null;
    }

    function set_x(name, value/*, ExpDate, path, domain*/)
    {
        let argv = arguments;
        let argc = arguments.length;
        let expires = (2 < argc) ? argv[2] : null;
        let path = (3 < argc) ? argv[3] : null;
        let domain = (4 < argc) ? argv[4] : null;
        let secure = (5 < argc) ? argv[5] : false;
        document.cookie = name + "=" + escape (value) +
            ((expires == null) ? "" : ("; expires=" + expires.toGMTString())) +
            ((path == null) ? "" : ("; path=" + path)) +
            ((domain == null) ? "" : ("; domain=" + domain)) +
            ((secure == true) ? "; secure" : "");
    }

    // 전달인자
    //   type  : 쿠키유형(쿠키가 사용되는 위치에 따라 결정)
    //   name  : 변수명(type에 따라 사용방법이 달라짐)
    //   value : 값(on:펼쳐짐, off:가려짐)
    //   expire : 만료 전 유지 시간(단위: 분). 디폴트 400일
    //   path  : 쿠키 적용 범위
    // 예) AMCookie.set('teacher_page_curriculum','gadje_t_lkh','on')
    //
    function set(type, name, value/*, expire, path, domain*/)
    {
        let argv = arguments;
        let argc = arguments.length;
        // [3] 인자는 만료시간(초) 값이다.
        let expires_period = -1;
        if( 3 < argc )
            expires_period = Math.round(1000*60*argv[3]);

        if( expires_period < 0 )
            expires_period = 1000*60*60*24*400; // 400일간 유지

        let ExpDate = new Date();
        ExpDate.setTime(ExpDate.getTime() + expires_period);

        // [4] 인자는 path이다.
        let pathname = location.pathname;
        let path = pathname.substring(0, pathname.lastIndexOf('/')) +'/';
        if(4 < argc)
        {
            path = argv[4];
            if( path.includes("./") )
                path = realpath(path)+"/";
        }

        // [5] 인자는 도메인이다.
        let domain = null;
        if(5 < argc)
            domain = argv[5];

        let cookie_name = type+'_'+name;
        if(type=="")
            cookie_name = name;
        else if(name=="")
            cookie_name = type;

        set_x(cookie_name, encodeURIComponent(value), ExpDate, path, domain);
    }

    function get_delete(type, name, default_value, path)
    {
        let return_val = get(type, name, default_value);
        del(type, name, path/*옵션*/);
        return return_val;
    }

    function in_array(needle, haystack)
    {
        for(let key in haystack)
        {
            if( haystack[key] == needle )
                return true;
        }
        return false;
    }

    function get(type, name, default_value = '', arrAllowable = [])
    {
        let cookie_name = type+'_'+name;
        if(type=="")
            cookie_name = name;
        else if(name=="")
            cookie_name = type;

        let cookie_value = get_x(cookie_name);
        if( cookie_value==null || cookie_value=='' )
            return default_value;

        let ret_val = decodeURIComponent(cookie_value);
        if( Array.isArray(arrAllowable) && arrAllowable.length > 0 )
        {
            if( !in_array(ret_val, arrAllowable) )
                ret_val = default_value;
        }

        return ret_val;
    }

    function get_list()
    {
        let objJson = {};
        let cookie = document.cookie.split(';');
        for(let i in cookie)
        {
            let cookieData = cookie[i].split('=');
            objJson[(cookieData[0]+'').trim()] = decodeURIComponent(unescape(cookieData[1]));
        }
        return objJson;
    }

    // 쿠키 삭제
    function del_x(type, name = '', path = ''/*옵션*/)
    {
        let ExpDate = new Date();

        let argv = arguments;
        let argc = arguments.length;
        // 과거 날짜로 설정한다.
        let expires_period = -1000*60*60*24*400; // 400일전 유지
        ExpDate.setTime(ExpDate.getTime() + expires_period);

        // [2] 인자는 도메인(적용범위)이다.
        if( path=='' )
        {
            let pathname = location.pathname;
            path = pathname.substring(0, pathname.lastIndexOf('/')) +'/';
        }

        let cookie_name = type+'_'+name;
        if(type=="")
            cookie_name = name;
        else if(name=="")
            cookie_name = type;

        set_x(cookie_name, "", ExpDate, path);
    }

    // 쿠키 삭제
    function del(type, name, path/*옵션*/)
    {
        let val = get(type, name, null);
        if( val!=null )
            del_x(type, name, path);
    }

    // 쿠키에서 사용할 목적으로 파일명을 얻는다.
    // 예) http://~~~/index.Test.html => index_test
    function get_current_file_name_for_cookie(pathname = '')
    {
        if( pathname=='' )
            pathname = location.pathname;

        let file_name = pathname.substring(pathname.lastIndexOf("/") + 1);
        let index_dot = file_name.lastIndexOf(".");
        let file_title = file_name.substring(0,index_dot).toLowerCase().replace(".","_");
        return file_title;
    }

    const YEAR		= 525600;	// 60*24*365
    const MONTH		= 43200;	// 60*24*30
    const DAY		= 1440;		// 60*24
    const HOUR		= 60;		// 60
    const MINUTE	= 1;		// 1

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    function setx(type, name, value/*, expire, path*/)
    {
        if( location.protocol=='file:' )
        {
            let cookie_name = type+'_'+name;
            window.localStorage.setItem(cookie_name, value);
        }
        else
        {
            let argv = arguments;
            let argc = arguments.length;

            if( argc >= 5 )
            {
                set(type, name, value, argv[3], argv[4]);
            }
            else if( argc >= 4 )
            {
                set(type, name, value, argv[3]/*, path*/);
            }
            else
            {
                set(type, name, value/*, expire, path*/);
            }
        }
    }

    function getx(type, name, default_value = '')
    {
        if( location.protocol=='file:' )
        {
            let cookie_name = type+'_'+name;
            let value = window.localStorage.getItem(cookie_name);
            if( value===null )
                return default_value;
            else
                return value;
        }
        else
        {
            return get(type, name, default_value);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // 세션 쿠키를 얻는다.
    function get_session(session_name = 'PHPSESSID')
    {
        return get(session_name, "", "");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // http://www.example.com/something/sub/../../else
    // -->
    // http://www.example.com/else
    function realpath(path)
    {
        //  discuss at: http://phpjs.org/functions/realpath/
        // original by: mk.keck
        // improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
        //        note: Returned path is an url like e.g. 'http://yourhost.tld/path/'
        //   example 1: realpath('../.././_supporters/pj_test_supportfile_1.htm');
        //   returns 1: 'file:/home/kevin/code/_supporters/pj_test_supportfile_1.htm'

        var p = 0,
            arr = []; /* Save the root, if not given */
        var r = window.location.href; /* Avoid input failures */
        path = (path + '')
            .replace('\\', '/'); /* Check if there's a port in path (like 'http://') */
        if (path.indexOf('://') !== -1) {
            p = 1;
        } /* Ok, there's not a port in path, so let's take the root */
        if (!p) {
            path = r.substring(0, r.lastIndexOf('/') + 1) + path;
        } /* Explode the given path into it's parts */
        var file_protocol = (path.indexOf('file://') !== -1);
        arr = path.split('/'); /* The path is an array now */
        path = []; /* Foreach part make a check */
        for (var k in arr) { /* This is'nt really interesting */
            if (arr[k] == '.') {
                continue;
            } /* This reduces the realpath */
            if (arr[k] == '..') {
                /* But only if there more than 3 parts in the path-array.
                 * The first three parts are for the uri */
                if (path.length > 3) {
                    path.pop();
                }
            } /* This adds parts to the realpath */
            else {
                /* But only if the part is not empty or the uri
                 * (the first three parts ar needed) was not
                 * saved */
                if (file_protocol)
                {
                    if ((path.length < 3) || (arr[k] !== '')) {
                        path.push(arr[k]);
                    }
                }
                else
                {
                    if ((path.length < 2) || (arr[k] !== '')) {
                        path.push(arr[k]);
                    }
                }
            }
        } /* Returns the absloute path as a string */

        var new_path = path.join('/');
        var origin = location.protocol + '//' + location.host;
        if( new_path.includes(origin) )
            new_path = new_path.substr(origin.length);

        return new_path;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // 노출할 public 메쏘드들
    return {
        // 일반 쿠키 함수
        'set'			: set,
        'get_delete'	: get_delete,
        'get'			: get,
        'get_list'		: get_list,
        'del'			: del,
        'get_current_file_name_for_cookie': get_current_file_name_for_cookie,
        // 상수 정의
        'YEAR'			: YEAR,
        'MONTH'			: MONTH,
        'DAY'			: DAY,
        'HOUR'			: HOUR,
        'MINUTE'		: MINUTE,
        // 세션
        'get_session'	: get_session,
        // 복합 쿠키(로컬+원격)
        'setx'			: setx,
        'getx'			: getx
    };
})();
window.AMCookie = AMCookie;

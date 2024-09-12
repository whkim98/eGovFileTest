<%--
  Created by IntelliJ IDEA.
  User: USER
  Date: 2024-09-12
  Time: 오후 1:43
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>단일 파일 업로드</title>
  <script src="/js/module.fileupload.js" type="module"></script>
  <script src="/js/cookie.js"></script>

</head>
<body>
<form id="uploadForm" action="/file/update" method="post" enctype="multipart/form-data">
  <input type="file" name="myfiles">
  <button type="button" onclick="submitFile()">업로드</button>
</form>

<script type="module">
  import { AmFileUpload } from '/js/module.fileupload.js';
  import { AMCookie } from '/js/cookie.js';


  AmFileUpload.initialize({
    objForm: '#uploadForm',
    server_url: '/file/update',
    fn_add: function(data) {
      console.log("파일이 추가되었습니다.", data);
    },
    fn_success: function(data) {
      alert("파일이 성공적으로 업로드되었습니다.");
    },
    file_upload_filter: {
      extension: {
        ext_list: 'png|jpg|jpeg|gif',
        error_msg: 'png, jpg, gif 파일만 업로드 가능합니다.'
      },
      filesize: {
        upload_max_filesize: 1048576,
        error_msg: '파일 크기는 1MB를 넘을 수 없습니다.'
      }
    }
  });

  function submitFile() {
    AmFileUpload.submit();
  }
</script>

</body>
</html>

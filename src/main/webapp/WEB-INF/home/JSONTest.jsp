<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>Title</title>
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
</head>
<body>
  <div>
    <form id="uploadForm" enctype="multipart/form-data">
    <table>
      <tr>
        <th>파일 선택</th>
        <td><input type="file" name="myfiles" id="myfiles" multiple></td>
      </tr>
      <tr>
        <td colspan="2">
          <button type="button" onclick="submitForm()">파일 업로드</button>
        </td>
      </tr>
    </table>
    </form>
    <table style="border: 1px solid">
      <c:forEach var="dto" items="${list}">
        <tr>
          <th>
            <button type="button" class="edit-btn" data-id="${dto.id}" data-filename="${dto.table_name}">
                ${dto.table_name}
            </button>
          </th>
          <td class="download-file" data-filename="${dto.table_name}">
              ${dto.table_name}
          </td>
          <td>
            <button type="button" class="history-btn" data-id="${dto.id}">이력조회</button>
          </td>
          <td>
            <button type="button" onclick="location.href='/file/delete?id=${dto.id}&&file_name=${dto.table_name}'">삭제</button>
          </td>
        </tr>
      </c:forEach>
    </table>
  </div>
  <script>
    function submitForm() {
      var formData = new FormData();
      var files = document.querySelector('input[type="file"]').files;

      // 파일 추가
      for (var i = 0; i < files.length; i++) {
        formData.append('myfiles', files[i]);
      }

      // 파일명 JSON 데이터 추가
      var fileNames = [];
      for (var i = 0; i < files.length; i++) {
        fileNames.push(files[i].name);  // 파일명만 추출
      }
      var jsonData = { fileNames: fileNames };
      formData.append('jsonData', new Blob([JSON.stringify(jsonData)], { type: 'application/json' }));

      // AJAX로 파일명과 파일을 서버로 전송
      $.ajax({
        url: '/json/file',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
          alert('업로드 성공: ' + JSON.stringify(response));
        },
        error: function(err) {
          alert('업로드 실패: ' + JSON.stringify(err));
        }
      });
    }
  </script>
</body>
</html>

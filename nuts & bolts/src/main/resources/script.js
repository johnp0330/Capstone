		//items in inventory
		
		var rIndex,
                table = document.getElementById("table"),
                server = "Application.java";

            $(window).on("load", function(){
				$.ajax({
					type: "POST",
					url: server,
					data: {inventory: 'read'},
					dataType: "json"
				}).done(function(data) {
					$.each(data, function (index, obj) {

						var eachrow = "<tr>"
									+ "<td class=itemname>"+obj.ItemName+"</td>"
									+ "<td class=sku>"+obj.SKU+"</td>"
									+ "<td class=price>"+obj.Price+"</td>"
									+ "<td class=quantity>"+obj.Quantity+"</td>"
									+ "<td class=description>"+obj.Description+"</td>"
									+ "</tr>";

						$('#table').append(eachrow);
					})
				}).fail(function(data) {
					$('p#tableStatus').text("Unable to load inventory.");
					$('p#tableStatus').css("color", "#b00020");
				}).always(function(data) {
					selectedRowToInput();
				});
            });
            
            function checkEmptyInput()
            {
                var isEmpty = false,
                    itemname = document.getElementById("itemname").value,
                    sku = document.getElementById("sku").value,
                    price = document.getElementById("price").value,
					quantity = document.getElementById("quantity").value,
					description = document.getElementById("description").value;
            
                if(itemname === ""){
                    alert("Item Name Cannot Be Empty");
                    isEmpty = true;
                }
                else if(sku === ""){
                    alert("SKU Cannot Be Empty");
                    isEmpty = true;
                }
                else if(price === ""){
                    alert("Price Cannot Be Empty");
                    isEmpty = true;
                }
				else if(quantity === ""){
                    alert("Quantity Cannot Be Empty");
                    isEmpty = true;
                }
				else if(description === ""){
                    alert("Description Cannot Be Empty");
                    isEmpty = true;
                }
                return isEmpty;
            }
            
            
            function addHtmlTableRow()
            {
                var newRow = table.insertRow(table.length),
                    cell1 = newRow.insertCell(0),
                    cell2 = newRow.insertCell(1),
                    cell3 = newRow.insertCell(2),
					cell4 = newRow.insertCell(3),
					cell5 = newRow.insertCell(4),
                    itemname = document.getElementById("itemname").value,
                    sku = document.getElementById("sku").value,
					price = document.getElementById("price").value,
					quantity = document.getElementById("quantity").value,
                    description = document.getElementById("description").value;
            
                cell1.innerHTML = itemname;
                cell2.innerHTML = sku;
                cell3.innerHTML = price;
				cell4.innerHTML = quantity;
				cell5.innerHTML = description;
                selectedRowToInput();
            }

            function selectedRowToInput()
            {
                for(var i = 1; i < table.rows.length; i++)
                {
                    table.rows[i].onclick = function()
                    {
                      rIndex = this.rowIndex;
                      document.getElementById("itemname").value = this.cells[0].innerHTML;
                      document.getElementById("sku").value = this.cells[1].innerHTML;
                      document.getElementById("price").value = this.cells[2].innerHTML;
					  document.getElementById("quantity").value = this.cells[3].innerHTML;
					  document.getElementById("description").value = this.cells[4].innerHTML;
                    };
                }
            }
            selectedRowToInput();
            
            function editHtmlTbleSelectedRow()
            {
				if(!checkEmptyInput()){
					var msg = "* Item failed to be edited.",
						color = "#b00020";

					$.ajax({
						type: form.attr('method'),
						url: form.attr('action'),
						data: form.serialize() + '&inventory=edit',
						contentType: "application/x-www-form-urlencoded"
					}).done(function(data) {
						if (data){
							var itemname = document.getElementById("itemname").value,
								sku = document.getElementById("sku").value,
								price = document.getElementById("price").value,
								quantity = document.getElementById("quantity").value,
								description = document.getElementById("description").value;

							table.rows[rIndex].cells[0].innerHTML = itemname;
							table.rows[rIndex].cells[1].innerHTML = sku;
							table.rows[rIndex].cells[2].innerHTML = price;
							table.rows[rIndex].cells[3].innerHTML = quantity;
							table.rows[rIndex].cells[4].innerHTML = description;
							msg = "Item edited.";
							color = "#77dd77";
						}
					}).always(function(data) {
						$('p#formStatus').text(msg);
						$('p#formStatus').css("color", color);
					});
				}
            }
            
            function removeSelectedRow()
            {
            	var msg = "* Item failed to be removed.",
					color = "#b00020";

				$.post(server, {inventory: "remove", sku: $('input#sku').val()},
					function(successful){
						if (successful)
						{
							msg = "Item removed.";
							color = "#77dd77";

							table.deleteRow(rIndex);
							document.getElementById("itemname").value = "";
							document.getElementById("sku").value = "";
							document.getElementById("price").value = "";
							document.getElementById("quantity").value = "";
							document.getElementById("description").value = "";
						}
				}).fail(function(data) {
					$('p#formStatus').text("Error " + msg);
				}).always(function(data) {
					$('p#formStatus').text(msg);
					$('p#formStatus').css("color", color);
				});
            }

			var form = $('form[name="itemForm"]');

			form.submit(function (e) {

				e.preventDefault();

				if (!checkEmptyInput())
				{
					var msg = "",
						color = "";

					$.ajax({
						type: form.attr('method'),
						url: form.attr('action'),
						data: form.serialize() + '&inventory=add',
						contentType: "application/x-www-form-urlencoded"
					}).done(function(data) {
						msg = "Item successfully added.";
						color = "#77dd77";
						addHtmlTableRow();
					}).fail(function(data) {
						msg = "* Item failed to be added.";
						color = "#b00020";
					}).always(function(data) {
						$('p#formStatus').text(msg);
						$('p#formStatus').css("color", color);
					});
				}
			});
			
			
			


			

			
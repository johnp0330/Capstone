 var rIndex,
                table = document.getElementById("table");
            
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
               
                if(!checkEmptyInput()){
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
                var itemname = document.getElementById("itemname").value,
                    sku = document.getElementById("sku").value,
					price = document.getElementById("price").value,
					quantity = document.getElementById("quantity").value,
                    description = document.getElementById("description").value;
               if(!checkEmptyInput()){
                table.rows[rIndex].cells[0].innerHTML = itemname;
                table.rows[rIndex].cells[1].innerHTML = sku;
                table.rows[rIndex].cells[2].innerHTML = price;
				table.rows[rIndex].cells[3].innerHTML = quantity;
				table.rows[rIndex].cells[4].innerHTML = description;
              }
            }
            
            function removeSelectedRow()
            {
                table.deleteRow(rIndex);
                document.getElementById("itemname").value = "";
                document.getElementById("sku").value = "";
                document.getElementById("price").value = "";
				document.getElementById("quantity").value = "";
				document.getElementById("description").value = "";
            }


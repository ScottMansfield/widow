#!/usr/local/bin/ruby

require "aws-sdk-core"

def truncate(dynamodb, table)
  items_to_delete = [ ]
  continue = true
  last_key = nil

  while continue do
    result = dynamodb.scan({
      table_name: table,
      attributes_to_get: ['ORIGINAL_URL', 'TIME_ACCESSED']
    })

    result[:items].each do |item|
      items_to_delete.push({
        'ORIGINAL_URL'  => item['ORIGINAL_URL'],
        'TIME_ACCESSED' => item['TIME_ACCESSED'].to_i
      })
    end

    if !result[:last_evaluated_key] || result[:last_evaluated_key].nil?
      continue = false
    end

    last_key = result[:last_evaluated_key]
  end

  while items_to_delete.length > 0 do
    request_items = [ ]

    items_to_delete.shift(25).each do |item|
      request_items.push({
        delete_request: { key: item }
      })
    end

    request_hash = {
      request_items: {
        "#{table}" => request_items
      }
    }

    # should have exponential backoff / retry
    dynamodb.batch_write_item request_hash
  end
end

dynamodb = Aws::DynamoDB::Client.new(region: 'us-west-2')

tables = dynamodb.list_tables
tables = tables[:table_names]

tables_to_truncate = [ ]
index = 1

while index != 0 do
  puts "\nAdd table to truncate list, 0 to stop, -1 for all:"

  print_index = 1
  tables.each do |table|
    puts print_index.to_s + ": " + table
    print_index = print_index.succ
  end

  puts "\nTable number:"
  index = gets.chomp.to_i

  if index == -1
   tables_to_truncate = tables
   break
  end

  if index != 0 && tables[index - 1]
    tables_to_truncate.push tables[index - 1]
  end

end

puts "\n"

tables_to_truncate.each do |table|
  puts "Truncating table #{table}, this might take a while"
  truncate dynamodb, table
end



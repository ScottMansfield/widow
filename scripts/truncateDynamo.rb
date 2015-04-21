#!/usr/local/bin/ruby

require "aws-sdk-core"

def truncate(dynamodb, table)
  items_to_delete = [ ]
  continue = true
  last_key = nil

  puts "Scanning table for keys"

  while continue do
    scan_request = {
      table_name: table,
      attributes_to_get: ['ORIGINAL_URL', 'TIME_ACCESSED']
    }

    if last_key
      scan_request[:exclusive_start_key] = last_key
    end

    result = dynamodb.scan scan_request

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

    puts "More keys to get. Last key: #{last_key}"
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

    puts "DELETING ITEMS: #{request_items}"

    # exponential backoff / retry
    tries = 0
    response = nil

    while tries < 3 do
      begin
        response = dynamodb.batch_write_item request_hash
        break
      rescue => e
        puts "Caught an exception: #{e.class.name}"
        sleep tries * tries
      end
    end

    if response[:unprocessed_items] && response[:unprocessed_items][table]
      response[:unprocessed_items][table].each do |item|
        key = item[:delete_request][:key]

        puts "Couldn't delete #{key}"

        items_to_delete.push key
      end
    end

    puts "ITEMS DELETED"

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



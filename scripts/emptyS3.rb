#!/usr/local/bin/ruby

require "aws-sdk-core"

def empty(s3, bucket)
  # get keys, iterate and delete
  keys = [ ]
  continue = true
  marker = nil

  while continue do
    request = { bucket: bucket }

    if !marker.nil?
      request[:marker] = marker
    end

    response = s3.list_objects request

    response[:contents].each do |item|
      keys.push item[:key]
    end

    if response[:is_truncated]
        if response[:next_marker]
          marker = response[:next_marker]
        else
          marker = keys[-1]
        end
    else
     continue = false
    end

    puts continue
    puts marker
  end

  while keys.length > 0 do
    request = {
      bucket: bucket,
      delete: { objects: [ ] }
    }

    keys.shift(1000).each do |key|
      request[:delete][:objects].push({ key: key })
    end

    # needs some retry
    s3.delete_objects request

  end

end

s3 = Aws::S3::Client.new(region: 'us-west-2')

buckets = s3.list_buckets
buckets = buckets[:buckets]

temp = buckets
buckets = [ ]

temp.each do |bucket|
  if bucket[:name].match /^widow/
    buckets.push bucket[:name]
  end
end

buckets_to_empty = [ ]
index = 1

while index != 0 do
  puts "\nAdd bucket to empty list, 0 to stop, -1 for all:"

  print_index = 1
  buckets.each do |bucket|
    puts print_index.to_s + ": " + bucket
    print_index = print_index.succ
  end

  puts "\nBucket number:"
  index = gets.chomp.to_i

  if index == -1
   buckets_to_empty = buckets
   break
  end

  if index != 0 && buckets[index - 1]
    buckets_to_empty.push buckets[index - 1]
  end

end

puts "\n"

buckets_to_empty.each do |bucket|
  puts "Emptying bucket #{bucket}, this might take a while"
  empty s3, bucket
end


#!/usr/local/bin/ruby

require "aws-sdk-core"

sqs = Aws::SQS::Client.new(region: 'us-west-2')

queue_urls = sqs.list_queues(queue_name_prefix: "widow-")[:queue_urls]

queues_to_purge = [ ]
index = 1

while index != 0 do
  puts "\nAdd queue to purge list, 0 to stop, -1 for all:"

  print_index = 1
  queue_urls.each do |queueName|
    puts print_index.to_s + ": " + queueName
    print_index = print_index.succ
  end

  puts "\nQueue number:"
  index = gets.chomp.to_i

  if index == -1
   queues_to_purge = queue_urls
   break
  end

  if index != 0 && queue_urls[index - 1]
    queues_to_purge.push queue_urls[index - 1]
  end

end

puts "\n"

queues_to_purge.each do |queue|
  puts "Purging #{queue}, it may take 60 seconds to clear"
  sqs.purge_queue queue_url: queue
end


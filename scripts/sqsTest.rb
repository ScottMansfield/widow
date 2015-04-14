#!/usr/local/bin/ruby

require "aws-sdk-core"

sqs = Aws::SQS::Client.new(region: 'us-west-2')

queue_urls = sqs.list_queues(queue_name_prefix: "widow-")[:queue_urls]

queue = nil
puts "Choose a queue:"

while queue.nil? do
  index = 0
  queue_urls.each do |queueName|
    puts index.to_s + ": " + queueName
    index = index.succ
  end

  puts "\nQueue number:"
  index = gets.chomp.to_i

  if queue_urls[index]
    queue = queue_urls[index]
  end

end


puts "\nWhat message?"
message = gets.chomp

puts "\nHow many times?"
times = gets.chomp.to_i

puts "\n\nSending message below #{times} times to queue #{queue}"
puts message

times.times do
  sqs.send_message(
    queue_url: queue,
    message_body: message
  )
  puts "sent"
end

